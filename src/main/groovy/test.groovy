import com.google.common.io.Files
@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7')

import groovyx.net.http.RESTClient

googleMaps = new RESTClient('https://maps.googleapis.com/maps/api/geocode/')

def getLocationForAddress(String address) {
    return googleMaps.get(
            path: 'json',
            query: [
                    key    : 'AIzaSyAulI8WKFpm68yn7BTUIALiEOunUmokP1Y',
                    address: address
            ]).responseData.results[0]?.geometry?.location;
}

boolean retrieveNextFewLocationsAndSave(File csvFile, int count) {
    File output = File.createTempFile("ctrlpkw",".csv");
    output.withWriter("cp1250") {
        csvFile.eachLine("cp1250") { line ->
            def fields = line.split(';') as List
            if (fields.size() == 13 && count > 0) {
                def location = getLocationForAddress(fields[7]);
                fields.add(location?.lat)
                fields.add(location?.lng)
                count--
            }
            it.writeLine(fields.join(';'))
        }
    }
    Files.move(output, csvFile)
    return count == 0;
}




File file = new File("pzt2010-obwody.csv")

while (retrieveNextFewLocationsAndSave(file,100)) {
    println "saved. retriving next locations"
}