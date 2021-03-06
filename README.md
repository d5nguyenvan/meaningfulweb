What is Meaningful Web?
=======================

We aim to extract structured information from a web resource:

url --> meaningfulweb engine --> structured information

### Artifacts:

1. meaningfulweb-core.jar <-- core engine
2. meaningfulweb-app.war  <-- web application

### Build:

Build and release are managed via Maven: http://maven.apache.org/

1. run the script: bin/mvn-install.sh to install .jar files in jars/ to local maven repo
2. build opengraph: under meaningfulweb-opengraph/, do: mvn install
3. build core: under meaningfulweb-core/, do: mvn install
4. start webapp: under meaningfulweb-app/, do: mvn jetty:run

application should be running at: http://localhost:8080/

the rest service should be running at: http://localhost:8080/get-meaning?url=xxx

Example:

http://localhost:8080/get-meaning?url=http://www.google.com


### Sample Code:

    // extract the best image representing an url

    String url = "http://www.google.com"

    MetaContentExtractor extractor = new MetaContentExtractor();
	OGObject obj = extractor.extractFromUrl(url);
	
    String bestImageURL = obj.getImage();
