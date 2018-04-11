<!DOCTYPE html>
<html>
<body>
<?php
   $con=mysqli_connect("fd8783.000webhostapp.com","id5262189_root","huihowai","id5262189_bookshop");

   if (mysqli_connect_errno($con)) {
      echo "Failed to connect to MySQL: " . mysqli_connect_error();
   }

   $result = mysqli_query($con,"SELECT eventImgURL FROM Event");
   $row = mysqli_fetch_array($result);
   $data = $row[0];

   if($data){
      echo $data;
   }
   mysqli_close($con);
?>
</body>
</html>