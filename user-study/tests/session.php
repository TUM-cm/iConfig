<?php
    session_start();

    if(isset($_SESSION)){
        echo "Session variable exists<br/>";

        if(!isset($_SESSION['test'])){
            $_SESSION['test'] = "Success!";
            echo "Variable has been set, refresh the page and see if stored it properly.";
        }else{
            echo $_SESSION['test'];
        }
    }else{
        echo "No session variable has been created.";
    }
?>