package com.zyden.stockhelper.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSFile;
import com.zyden.stockhelper.datasource.GoogleVisionAPI;
import com.zyden.stockhelper.datasource.IVisionAPI;
import com.zyden.stockhelper.model.ImageFile;
import com.zyden.stockhelper.model.Stock;
import com.zyden.stockhelper.model.Thing;
import com.zyden.stockhelper.model.UserThing;
import com.zyden.stockhelper.repo.ThingRepo;
import com.zyden.stockhelper.repo.UserThingRepo;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Kenny on 4/18/2017.
 */

@CrossOrigin(origins = "http://localhost:8889")
@RestController
@RequestMapping("/api/v1/things")
public class ThingController {

    @Autowired
    private ThingRepo thingRepo;

    @Autowired
    private IVisionAPI visionAPI;

    @Autowired
    private GridFsOperations gridFsOperations;

    private Log log =  org.apache.commons.logging.LogFactory.getLog(this.getClass());

    @Autowired
    private UserThingRepo userThingRepo;

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody List<UserThing> getUserThings(){
        return userThingRepo.findAll();
    }

    @RequestMapping(method = RequestMethod.GET,value="/{imageId}")
    public @ResponseBody Thing getThing(@PathVariable String imageId){
        return thingRepo.findBySha256(imageId);
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Thing addThing(@RequestBody Thing newThing){
        Thing saved = thingRepo.save(newThing);
        return saved;
    }

    @RequestMapping(method = RequestMethod.POST, value="/upload")
    public @ResponseBody Thing uploadThing(@RequestParam(value="image", required=true) MultipartFile file) {
        if (file.isEmpty()) {
            log.error("Empty file uploaded");
            throw new java.lang.RuntimeException("Empty file");
        }
        Thing t = new Thing();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] sha256 = md.digest(file.getBytes());
            String id =  org.apache.commons.codec.binary.Hex.encodeHexString(sha256);

            if(thingRepo.exists(id)){
                log.info("Using existing Thing:" + id);
                t = getThing(id);
            }
            else {
                InputStream inputStream = file.getInputStream();
                DBObject metaData = new BasicDBObject();
                metaData.put("fileName", file.getOriginalFilename());
                metaData.put("fileSize", file.getSize());

                gridFsOperations.store(inputStream, id, file.getContentType(), metaData);

                Map<String, BigDecimal> labels = visionAPI.getLabels(file.getBytes());

                t.setId(id);
                t.setSha256(id);
                t.setLabelScore(labels);
                t.setDateAdded(new Date());
                t.setThumbnailPngBase64(generateThumbnail());
                t = thingRepo.save(t);
            }
        } catch (IOException e) {
            log.error("Unable to create ImageFile object", e);
            throw new RuntimeException("Upload failed");
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to generate SHA-256 for ImageFile object", e);
            throw new RuntimeException("Upload failed");
        }
        //Auto create a UserThing repo relationship
        createUserThing(t);
        return t;
    }

    private void createUserThing(Thing t) {
        String currentUser = "0";

        UserThing ut = new UserThing();
        ut.setLastModified(new Date());
        ut.setOwnerId(currentUser);
        ut.setThing(t);
        userThingRepo.save(ut);

    }

    @RequestMapping(value="/images/{imageId}")
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
                } catch(Exception e) {}
            }
        } else {
            result = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return result;
    }

    private String generateThumbnail() {

        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAZAAAADSCAMAAABThmYtAAAAXVB";
    }
}
