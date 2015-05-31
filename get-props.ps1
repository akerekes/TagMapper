param (
    [string]$file = "c:\work\Katanak\TagMapper\Doc1.docx"
 )

$application = New-Object -ComObject word.application
$application.Visible = $false
$document = $application.documents.open($file)
$binding = "System.Reflection.BindingFlags" -as [type]
$properties = $document.BuiltInDocumentProperties
foreach($property in $properties)
{
 $pn = [System.__ComObject].invokemember("name",$binding::GetProperty,$null,$property,$null)
  trap [system.exception]
   {
#     write-host -foreground blue "Value not found for $pn"
    continue
   }
 if ($pn -eq "Keywords") {
  "$pn`: " +
   [System.__ComObject].invokemember("value",$binding::GetProperty,$null,$property,$null)
 }
}
$application.quit()