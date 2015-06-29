#!/usr/bin/php            
<?php
	error_reporting(0);
	echo "\n";
	$codeactive = 0;
	$codetype="";
	$noteactive = 0;
	$tableactive = 0;
	$imagelink= "";
	$newlines=0;
	$listnumber=1;
	require_once "spyc.php";
	$tocarray = Spyc::YAMLLoad('guide/toc.yml');
	
	while (($gdoc=fgets(STDIN, 256*1024)) !== false){ 
		$gdoclength= strlen($gdoc);
		for($i=0; $i<$gdoclength; $i++){
			$echo=0;
		   
			//numbered list
			if($gdoc[$i]=="\n"){$newlines++;}
			if($codeactive==0){
				if($gdoc[$i]=="#"){
					if($newlines!=$newlinesold+1){$listnumber=1;}
					echo $listnumber . ".";
					$echo=1;
					$listnumber++;
					$newlinesold=$newlines;
					
				}
			}
		   //check if {code} is active
		   if($gdoc[$i]=="{"){
				$tag = substr($gdoc,$i,5);
				if ($tag=="{code"){
					if($codeactive==1){
						$codeactive=0;
					}
					else{
						$codeactive=1;
					}
				}
			}

			//replace @ with ' outside {code}
			if ($codeactive==0){
				if($gdoc[$i]=="@"){
					$apos = html_entity_decode("\'");
					echo "_";
					$echo=1;
				}
			}
			
			
			//replace {code} with ----
			if($gdoc[$i]=="{"){
				$tag = substr($gdoc,$i,5);
				if ($tag=="{code"){
					$tagtwo = substr($gdoc,$i+6,4);
					if($tagtwo=="html"){
							echo "[source,html]\n----\n";
							$i = $i+11;
							$echo=1;
							break;
						}
					else if($tagtwo=="java"){
							echo "[source,java]\n----\n";
							$i = $i+11;
							$echo=1;
							break;
						}
					else if($tagtwo=="xml}"){
							echo "[source,xml]\n----\n";
							$i = $i+10;  
							$echo=1; 
							break; 
						}
					else if ($codeactive==1){
							echo "[source,java]\n----\n";
							$i = $i+6;
							$echo=1;
							break;
						}       
					else{    
							echo "----\n";
							$i = $i+6;
							$echo=1;
							break;
						}
				}       
			}
				
			
			
			//replace {note} with NOTE:
			if($gdoc[$i]=="{"){
				$tag = substr($gdoc,$i,6);
				if($tag=="{note}"){
						if($noteactive==0){ 
							echo "NOTE: ";
							$echo=1;
							$i = $i+7;
							$noteactive=1;
							break;

						}
						else{
							$noteactive=0;
							$i=$i+6;
							$echo=1;
							break;
						}
				}
			}
			//replace {warning} with WARNING:
			if($gdoc[$i]=="{"){
				$tag = substr($gdoc,$i,9);
				if($tag=="{warning}"){
						if($noteactive==0){ 
							echo "WARNING: ";
							$noteactive=1;
							$echo=1;
							$i = $i+10;
							break;

						}
						else{
							$noteactive=0;
							$i=$i+9;
							$echo=1;
							break;
						}
				}
			}
			//replace [WEBSITE|http://www.WEBSITE.COM/] with http://WEBSITE.COM[WEBSITE]
			if($codeactive==0){
				if($gdoc[$i]=="["){
					$link="";
					$linkname="";
					$templink="";
					$linkplaced=0;
					for($i2=0;$gdoc[$i+$i2]!="]" && $i2<200; $i2++){
						if($gdoc[$i+$i2]=="|"){
							$link= $templink;
							$link=substr_replace($link, "", 0, 1);
							$templink = "";
							for($i3=0;$gdoc[$i+$i2+$i3]!="]" && $i3<200; $i3++){
								$templink = $templink . $gdoc[$i+$i2+$i3];
							}
							$templink=substr_replace($templink, "", 0,1);
							$linkname= $templink;
						}
						$templink= $templink . $gdoc[$i+$i2];
					}
					if($link==""){
						$link=$templink;
						$link=substr_replace($link, "", 0,1);
					}
					if($linkname==""){
					$linkvalidated = filter_var($link, FILTER_SANITIZE_URL);
					if (!filter_var($linkvalidated, FILTER_VALIDATE_URL) === false) {
							$linklength = strlen($link) + strlen($linkname) + 2;
							echo $link . "\n";
							$echo=1;
							$i=$i+$linklength;
							$linkplaced=1;
							break;
						}
					}else{
						$linknamevalidated = filter_var($linkname, FILTER_SANITIZE_URL);
						if (!filter_var($linknamevalidated, FILTER_VALIDATE_URL) === false) {
							$linklength = strlen($link) + strlen($linkname) + 2;
							echo "$linkname" . "[" . $link. "]\n";
							$echo=1;
							$i=$i+$linklength;
							$linkplaced=1;
							break;
						}
					}		
					if($linkplaced==0){
						if($linkname!=""){
							$linknamelength=strlen($linkname);
							$linkname=substr($linkname,6,$linknamelength-6);
							$linknamelength=strlen($linkname);
							if($linkname[$linknamelength-2]=="_"){
								$linkbase=substr($linkname,0,$linknamelength-2);
								foreach($tocarray as $key => $value){
									if($key==$linkbase){
										foreach($tocarray["$key"] as $key2 => $value2){
											if($key2==$linkname){
												$value2=str_replace(" - ","-",$value2);
												$value2=str_replace("   ","-",$value2);
												$value2=str_replace("  ","-",$value2);
												$value2=str_replace(" ","-",$value2);
												$value2=strtolower(ereg_replace("[^A-Za-z-]", "",$value2));
												echo "<<$linkbase.adoc#$value2,$link>>";
												$echo=1;
												$linklength=strlen($link);
												$i=$i+$linklength+$linknamelength+8;
											}
										}
									}
								}
							}else{
								foreach($tocarray as $key => $value){
									if($key==$linkname){
										$value2=$tocarray[$key]["title"];
										$value2=str_replace(" - ","-",$value2);
										$value2=str_replace("   ","-",$value2);
										$value2=str_replace("  ","-",$value2);
										$value2=str_replace(" ","-",$value2);
										$value2=strtolower(ereg_replace("[^A-Za-z-]", "",$value2));
										echo "\n<<$value2,$link>>";
										$echo=1;
										$linklength=strlen($link);
										$i=$i+$linklength+$linknamelength+8;
									}
								}
							}
						}else{
							$linklength = strlen($link) + strlen($linkname) + 1;
							echo " <<$link>>\n";
							$echo=1;
							$i=$i+$linklength;
						}
					}
				}
			}
			//tables
			if($codeactive==0){
				$tag = substr($gdoc,$i,7);
				if($tag=="{table}"){
					if($tableactive==0){
						$tableactive = 1;
						echo "|===\n|";
						$echo=1;
						$i=$i+7;
						break;
					}
					else {
						$tableactive=0;
						echo "===\n";
						$echo=1;
						$i=$i+7;
						break;
					}
					
				}
			}
			if($tableactive==1) {
				if($gdoc[$i]=="\n"){
					echo"\n|";
					$echo=1;
				}
			}
			//headings
			if($codeactive==0){
				$tag = substr($gdoc,$i,4);
				if($tag=="h1. "){
					echo "= ";
					$echo=1;
					$i=$i+3;
				}
				if($tag=="h2. "){
					echo "== ";
					$echo=1;
					$i=$i+3;
				}
				if($tag=="h3. "){
					echo "=== ";
					$echo=1;
					$i=$i+3;
				}
				if($tag=="h4. "){
					echo "==== ";
					$echo=1;
					$i=$i+3;
				}
				if($tag=="h5. "){
					echo "===== ";
					$echo=1;
					$i=$i+3;
				}
				if($tag=="h6. "){
					echo "====== ";
					$echo=1;
					$i=$i+3;
				}
			}
			
			//images
			if($codeactive==0){
				if($gdoc[$i]=="!"){
						$noimage=0;
						$imagelink= "";
						for($i2=1;$i2<200;$i2++){
							if($gdoc[$i+$i2]=="!"){break;}
							if($i2>198){$noimage=1;break;}
							if($gdoc[$i+$i2]==" "){$noimage=1;break;}
							if($gdoc[$i+$i2]=="*"){$noimage=1;break;}
							$imagelink= $imagelink . $gdoc[$i+$i2];
						}
						if($noimage==0){
							echo "image::../img/$imagelink" . "[]\n";
							$echo= 1;
							$imagelinklength= strlen($imagelink);
							$i=$i+$imagelinklength+2;
							break;
						}
				}
			}
			//convert links like: "example":http://example.com
			if($codeactive==0){
				if($gdoc[$i]=="\""){
					$linknamefound=0;
					$tempvar="";
					$linkname="";
					$linkadress="";
					$linkimage="";
					for($i2=1;$i2<200;$i2++){
						if($gdoc[$i+$i2]=="\""){$linkname = $tempvar; $tempvar= "";$linknamefound=1;}
						if($linknamefound==1){if($gdoc[$i+$i2]==" " || $gdoc[$i+$i2]=="\n"){$linkadress = $tempvar; $tempvar=""; break;}}
						$tempvar = $tempvar . $gdoc[$i+$i2];
					}
					if($linkname[0]=="!"){
						$linknamelength=strlen($linkname);
						$linkimagelength= strlen($linkname);
						$linkadresslength=strlen($linkadress);
						echo "\n\nimage::../img/" . substr($linkname, 1, $linkimagelength-2) . "[link=\"" . substr($linkadress, 2, $linkadresslength-2) . "\"]";
						$i = $i + $linkadresslength + $linknamelength + 2;
					}
					else{
						$linkadresslength=strlen($linkadress);
						$linknamelength=strlen($linkname);
						echo " " . substr($linkadress, 2, $linkadresslength-2) . "[" . $linkname . "] ";
						$i = $i + $linkadresslength + $linknamelength + 2;
					}
				}
			}
			
			if($echo==0){
				echo $gdoc[$i];
			}
			$gdoclength= strlen($gdoc);
		}
   }
   echo "\n";
					   
?>
