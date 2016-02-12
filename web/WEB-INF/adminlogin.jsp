<!DOCTYPE html>
<html>
    <head>
        <link rel="stylesheet" href="../theme.css">
        <link rel="stylesheet" href="../loginregister.css">
        <title>Login</title>
    </head>
    <body>



        <div id = "header">

            <h1>
                MyChat <small>Admin</small>
            </h1>

        </div>


        <div id="content">
            <h2>Sign In</h2>
            <p id="status"></p>	
            <form action="processLogin" method="post">
                <label for="username">Username: </label>
                <input class ="txtin" type="text" id="username" name="username" required="required" maxlength=30 />
                <br />
                <label for="password">Password: </label>
                <input class="txtin" type="password" id="password" name="password" required="required" maxlength=30 />
                <br />
                <button class = "btn btn-default" type ="submit" id ="btn">Submit</button>
            </form>
        </div>
  
        <script type="text/javascript" src="../jquery-1.12.0.min.js"></script>
        <script type="text/javascript" src = "../client.js"></script>
        <script type="text/javascript" src="../loginregister.js"></script>
        <script>

            $('form').on('submit', function (e) {
                e.preventDefault();
                submitFormAjax('processLogin', $('#status'), 'messages');
            });
        </script>
    </body>
</html>