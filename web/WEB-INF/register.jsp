<!DOCTYPE html>
<html>
    <head>
              <base href="../" />
        <link rel="stylesheet" href="theme.css">
        <link rel="stylesheet" href="loginregister.css">
        <title>Register</title>
    </head>
    <body>

        <div id = "header">
            <h1>
                MyChat <small>Userspace</small>
            </h1>

        </div>


        <div id="content">
            <h2>Create Account</h2>
            <p id="status"></p>	
            <form action="web/processlogin" method="post">
                <label for="username" >Username: </label>
                <input class ="txtin" id="username" type="text" name="username" required="required" maxlength=30 />
                <br />
                <label for="email" >Email: </label>
                <input class ="txtin" type="text" id="email" name="email" required="required" maxlength=30 />
                <br />
                <label for="password" >Password: </label>
                <input class="txtin" type="password" id="password" name="password" required="required" maxlength=30 />
                <br />
                <input type="hidden" name="agent" value="false" /> 
                <button class = "btn btn-default" type ="submit" id ="btn">Submit</button>
            </form>
            <p>Already have an account? <a href="web/login">Login</a></p>
        </div>

        <script type="text/javascript" src="jquery-1.12.0.min.js"></script>
        <script type="text/javascript" src = "client.js"></script>
        <script type="text/javascript" src="loginregister.js"></script>
        <script>
 
            $('form').on('submit', function (e) {
                e.preventDefault();
                submitFormAjax('processRegister', $('#status'), 'login');
            });
        </script>
    </body>
</html>