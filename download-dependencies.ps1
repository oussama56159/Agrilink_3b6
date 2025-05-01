$dependencies = @(
    @{
        GroupId = "com.google.api-client"
        ArtifactId = "google-api-client"
        Version = "2.2.0"
    },
    @{
        GroupId = "com.google.oauth-client"
        ArtifactId = "google-oauth-client"
        Version = "1.34.1"
    },
    @{
        GroupId = "com.google.http-client"
        ArtifactId = "google-http-client"
        Version = "1.43.3"
    },
    @{
        GroupId = "com.google.http-client"
        ArtifactId = "google-http-client-gson"
        Version = "1.43.3"
    },
    @{
        GroupId = "com.google.oauth-client"
        ArtifactId = "google-oauth-client-jetty"
        Version = "1.34.1"
    },
    @{
        GroupId = "com.google.apis"
        ArtifactId = "google-api-services-oauth2"
        Version = "v2-rev20200213-2.0.0"
    },
    @{
        GroupId = "com.google.code.gson"
        ArtifactId = "gson"
        Version = "2.10.1"
    }
)

$baseUrl = "https://repo1.maven.org/maven2"
$outputDir = "lib\google-api-client"

if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir
}

foreach ($dep in $dependencies) {
    $groupIdPath = $dep.GroupId -replace '\.', '/'
    $filename = "$($dep.ArtifactId)-$($dep.Version).jar"
    $url = "$baseUrl/$groupIdPath/$($dep.ArtifactId)/$($dep.Version)/$filename"
    $outputPath = Join-Path $outputDir $filename
    
    Write-Host "Downloading $filename..."
    Invoke-WebRequest -Uri $url -OutFile $outputPath
}

Write-Host "All dependencies downloaded successfully!" 