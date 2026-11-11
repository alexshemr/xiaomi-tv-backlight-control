param([Parameter(Mandatory)][string]$Sdk, [Parameter(Mandatory)][string]$Jdk)
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = $Jdk
function Invoke-BuildTool($Tool, $Arguments) {
    & $Tool @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Tool exited with $LASTEXITCODE" }
}
Push-Location $PSScriptRoot
try {
    $tools = Join-Path $Sdk 'build-tools/34.0.0'
    $platform = Join-Path $Sdk 'platforms/android-34/android.jar'
    $qr = Join-Path $PSScriptRoot 'build/deps/zxing-core-3.5.3.jar'
    if (!(Test-Path $qr)) {
        New-Item -ItemType Directory -Force (Split-Path $qr) | Out-Null
        Invoke-WebRequest 'https://repo.maven.apache.org/maven2/com/google/zxing/core/3.5.3/core-3.5.3.jar' -OutFile $qr
    }
    if ((Get-FileHash $qr -Algorithm SHA256).Hash -ne '8D8064C1636FDAEF7189DD9055C7D59950A8940A12F2293956446EC3C109FD82') {
        throw 'ZXing checksum mismatch'
    }
    New-Item -ItemType Directory -Force build/classes,build/dex,build/generated | Out-Null
    Invoke-BuildTool "$tools/aapt.exe" @('package','-f','-m','-M','AndroidManifest.xml','-S','res','-A','assets','-I',$platform,'-J','build/generated')
    Invoke-BuildTool "$Jdk/bin/javac.exe" (@('-encoding','UTF-8','--release','8','-classpath',"$platform;$qr",'-d','build/classes') + @(Get-ChildItem src/*.java,build/generated/local/ha/tvbacklight/R.java | ForEach-Object FullName))
    Invoke-BuildTool "$tools/d8.bat" (@('--lib',$platform,'--min-api','26','--output','build/dex',$qr) + @(Get-ChildItem build/classes -Recurse -Filter *.class | ForEach-Object FullName))
    Invoke-BuildTool "$tools/aapt.exe" @('package','-f','-M','AndroidManifest.xml','-S','res','-A','assets','-I',$platform,'-F','build/unsigned.apk')
    Push-Location build/dex
    try { Invoke-BuildTool "$tools/aapt.exe" @('add','../unsigned.apk','classes.dex') } finally { Pop-Location }
    Invoke-BuildTool "$tools/zipalign.exe" @('-f','4','build/unsigned.apk','build/aligned.apk')
    if (!(Test-Path build/local-signing.p12)) {
        Invoke-BuildTool "$Jdk/bin/keytool.exe" @('-genkeypair','-keystore','build/local-signing.p12','-storepass','localbuild','-keypass','localbuild','-alias','tv','-dname','CN=Local HA TV Backlight','-keyalg','RSA','-validity','10000')
    }
    Invoke-BuildTool "$tools/apksigner.bat" @('sign','--ks','build/local-signing.p12','--ks-pass','pass:localbuild','--out','build/tv-backlight.apk','build/aligned.apk')
    Invoke-BuildTool "$tools/apksigner.bat" @('verify','--min-sdk-version','26','build/tv-backlight.apk')
} finally { Pop-Location }




