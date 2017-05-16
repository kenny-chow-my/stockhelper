const LoginForm = () => (
<form action="/signin/twitter" method="post">
    <h1>Please login</h1>
<button type="submit">Twitter</button>
    </form>
);

const LogoutComponent = (props) => (
<div>
<h2>Your name is {props.name}</h2>
<button onClick={props.logout}>Logout</button>
</div>
);

class Main extends React.Component {

    constructor(...args) {
    super(...args);
    this.state = {username: null};
}

componentDidMount() {
    fetch('/api/v1/session', {credentials: 'same-origin'})
        .then(res => {
            console.log(res);
            return res.json();
        })
.then(session => {
    console.log(session);
    this.setState({username: session.username});
});
}

logout() {
    console.log("logout");
    fetch('/api/v1/session', {method: 'delete', credentials: 'same-origin'})
        .then(res => this.setState({username: null}));
}

render() {
    const profile = this.state.username ?
<LogoutComponent name={this.state.username} logout={() => this.logout()}/> :
<LoginForm />;
    return (
        <div>
        {profile}
        </div>
)
}
}

ReactDOM.render(<Main />, document.getElementById('container'));