#!/usr/bin/php            
            <?php	
					$leveloffset=0;
					echo "\n";
					echo ":sectnums:\n\n";
					require_once "spyc.php";
					$tocarray = Spyc::YAMLLoad('guide/toc.yml');
					
					foreach($tocarray as $key => $value){
						$title= $tocarray[$key]["title"];
						echo "== $title\n\ninclude::$key.adoc[]\n\n";
						foreach($tocarray["$key"] as $key2 => $value2){	
							if($key2!="title"){
								echo "=== $value2\n\n";
								echo "include::$key/$key2.adoc" . "[leveloffset=+1]\n\n";
							}
						}
					}   				   			
			   echo "\n";
 						           
            ?>
