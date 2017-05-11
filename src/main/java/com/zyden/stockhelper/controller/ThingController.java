package com.zyden.stockhelper.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.zyden.stockhelper.datasource.IVisionAPI;
import com.zyden.stockhelper.model.ImageFile;
import com.zyden.stockhelper.model.Thing;
import com.zyden.stockhelper.model.UserThing;
import com.zyden.stockhelper.repo.ThingRepo;
import com.zyden.stockhelper.repo.UserThingRepo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

/**
 * Created by Kenny on 4/18/2017.
 */

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/things")
public class ThingController {

    @Autowired
    private ThingRepo thingRepo;

    @Autowired
    private IVisionAPI visionAPI;

    @Autowired
    private GridFsOperations gridFsOperations;

    private Log log = org.apache.commons.logging.LogFactory.getLog(this.getClass());

    @Autowired
    private UserThingRepo userThingRepo;

    @Value("${wipeall.password}")
    private String wipeAllProp;


    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    List<UserThing> getUserThings() {
        return userThingRepo.findAll();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{imageId}")
    public @ResponseBody
    Thing getThing(@PathVariable String imageId) {
        return thingRepo.findBySha256(imageId);
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody
    Thing addThing(@RequestBody Thing newThing) {
        Thing saved = thingRepo.save(newThing);
        return saved;
    }

    @RequestMapping(method = RequestMethod.PUT)
    public @ResponseBody
    UserThing updateUserThing(@RequestBody UserThing userThing) {
        UserThing saved = userThingRepo.save(userThing);
        return saved;
    }


    @RequestMapping(value="/wipeall" , method = RequestMethod.DELETE)
    public @ResponseBody String wipeEverything(@RequestBody String password) {
        if(password == this.wipeAllProp){
            log.warn("Wiping all database!");
            userThingRepo.deleteAll();
            thingRepo.deleteAll();
            return "Wiped all";
        }

        return "No action: " + password;

    }
    @RequestMapping(method = RequestMethod.DELETE)
    public void deleteUserThing(@RequestBody String thingId) {
        if(!thingRepo.exists(thingId)){
            log.error("Cannot find ThingId: " + thingId);
            throw new java.lang.RuntimeException("No such Thing: " + thingId);
        }
        List<UserThing> userthings = userThingRepo.findAll();
        for(UserThing userthing : userthings){
            if(userthing.getThing() == null || userthing.getThing().getId() == thingId){
                userThingRepo.delete(userthing.getId());
            }
        }
        thingRepo.delete(thingId);

    }

    @RequestMapping(method = RequestMethod.POST, value = "/upload")
    public @ResponseBody
    UserThing uploadThing(@RequestBody ImageFile imageFile) {
        if (imageFile == null || imageFile.getImageDataBase64().isEmpty()) {
            log.error("Empty file uploaded");
            throw new java.lang.RuntimeException("Empty file");
        }
        Thing t = new Thing();
        try {
            byte[] imageData = stripDataEncoding(imageFile.getImageDataBase64());

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] imgDataSHA256 = md.digest(imageData);
            String id = org.apache.commons.codec.binary.Hex.encodeHexString(imgDataSHA256);

            if (thingRepo.exists(id)) {
                log.info("Using existing Thing:" + id);
                t = getThing(id);
            } else {
                //InputStream inputStream = file.getInputStream();
                DBObject metaData = new BasicDBObject();
                metaData.put("fileName", imageFile.getFilename());
                metaData.put("fileSize", imageData.length);
                metaData.put("fileType", imageFile.getContentType());

                gridFsOperations.store(new ByteArrayInputStream(imageData), id, metaData);

                Map<String, BigDecimal> labels = visionAPI.getLabels(imageData);

                t.setId(id);
                t.setSha256(id);
                t.setLabelScore(labels);
                t.setDateAdded(new Date());
                t.setThumbnailPngBase64(generateThumbnail(imageData));
                t = thingRepo.save(t);
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to generate SHA-256 for ImageFile object", e);
            throw new RuntimeException("Upload failed");
        }
        //Auto create a UserThing repo relationship
        return createUserThing(t);

    }

    private byte[] stripDataEncoding(String imgDataBase64) {
        //Strip the base64 front of image data e.g. data:image/jpeg;base64,/9j/4AA...
        String encodingPrefix = "base64,";
        int contentStartIndex = imgDataBase64.indexOf(encodingPrefix) + encodingPrefix.length();
        return Base64.decodeBase64(imgDataBase64.substring(contentStartIndex));
    }

    private UserThing createUserThing(Thing t) {
        String currentUser = "0";

        //TODO: allow user to select labels
        // for now, just select everything by default
        List<String> labels = new ArrayList(t.getLabelScore().keySet());

        UserThing ut = new UserThing();
        ut.setLastModified(new Date());
        ut.setOwnerId(currentUser);
        ut.setThing(t);
        ut.setSelectedLabels(labels);
        ut.setThumbnailDataURI(t.getThumbnailPngBase64());
        return userThingRepo.save(ut);

    }

    @RequestMapping(value = "/images/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageId) {
        GridFsResource resource = gridFsOperations.getResource(imageId);

        ResponseEntity<byte[]> result = null;
        if (resource != null) {
            InputStream inputStream = null;
            try {
                inputStream = resource.getInputStream();
                MultiValueMap<String, String> headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_TYPE, resource.getContentType());

                result = new ResponseEntity<>(StreamUtils.copyToByteArray(inputStream), headers, HttpStatus.OK);
            } catch (IOException e) {
                log.error("Unable to retrieve image", e);
                result = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } finally {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        } else {
            result = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return result;
    }

    private String generateThumbnail(byte[] original) {
        ByteArrayInputStream bais = new ByteArrayInputStream(original);
        String imageString = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAJwAAAB7BAMAAABz1/REAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsQAAA7EAZUrDhsAAAAwUExURdbW1tnZ2dra2t3d3eDg4OPj4+np6erq6u3t7e/v7/Ly8vb29vf39/n5+fz8/P///4yuoWoAAAHvSURBVGje7dgxT8JQEAfwYnSQqRR1MDJ00BC26tjFRuOkg6uLi3FxMH4CxS/A4s6uQ2MwcSDahb2DxkEHZjWRBaIB9KSlgVICvb6eiSH335qmvzyur497T0qRRmKOOeaYY4455pj7M07eOgzLQQLPZasQlu9zPBeuAbQ0LLcEmFxjuVMU10BysoniWgkkZwE8X47PVedlROBkaXyma5E4NWS6pqvwM1HcXg1eNTJuxZkNTyoVV3Bn1wkRt9CdrHUibsdbOYi4Y+9b2vXdSYhzBY/zF+9CnLM8Lt+/kWlrsbmz/ohseBDmikM/VgdoG3Svwu5c3cSdKL2HMu6lEW8aN3yVc/IoyCnFwRere6M1BL/ZrPP0p+qvnJOK6AK1DfCmDlSuPzwBTp7bkAKV61Uv5mqs+9oJIz5n+/5gK7G5zEC3Ywhzy1qgcl71xDjFLAUq5w1PjFuEVrBy3eEJcUqn+SkFKuekqQpxObctTNvDraIQ53ZmJR1ouKT7bNsi4szRfbEAlwNSziTlkkDJKSYplwNSrkDLWcwxh+GOyqNzG301Tq+NzupkbfHWQ7bHM9G2xy/l8bmLsHnHHS00kVxqH8XVsecosyguj+WUIkL7wp9Bzd9/hOXdiHB+N7UZFpkPK5ljjjnmmGOOOeb+JfcLsEDe7STib2kAAAAASUVORK5CYII=";
        try {
            Image img = ImageIO.read(bais).getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedImage buffered = new BufferedImage(100, 100, TYPE_INT_RGB);
            Graphics2D bGr = buffered.createGraphics();
            bGr.drawImage(img, 0, 0, null);
            bGr.dispose();

            ImageIO.write(buffered, "png", baos);
            imageString = "data:image/png;base64," +  Base64.encodeBase64String(baos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }
}
