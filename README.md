# Co-Simulation of Intralogistic

## Model Simulation and Verification 

This project allow to simulate and formally verify a process defined in one of the following format:
- BPMN Standard
- ADOxx XML export of BPMN library
- ADOxx XML export of BPMN BeeUp library
- ADOxx XML export of Petri Net BeeUp library
- ADO XML Light
- PNML Standard

The input model can be provided:
- Via the `Choose a model` button in the UI
- Via the querystring parameter `modelURL` in case the model is available in a public URL
- Via JS `window.postMessage()` in case the simulation/verification page is integrated as iFrame

The following REST APIs are available accepting the XML model as input and returning results as output in XML:
- POST /rest/simulator/pathanalysis
- POST /rest/verificator/deadlock
- POST /rest/verificator/unboundness
- POST /rest/verificator/reachability?bpObjectId=
- POST /rest/verificator/path?bpFromObjectId=&bpToObjectId=

### Important Notes:

- If the war file name is changed, the files verificator.js and simulator.js must be adapted


### Instructions to build and start the container:

1) Build the image
<pre>
sudo docker build --no-cache -t model-simulation-verification .
</pre>

2) Run the container
<pre>
sudo docker run -d -p 8080:8080 --name model-simulation-verification --restart always model-simulation-verification
</pre>

3) Access the Model Simulator at http://127.0.0.1:8080/model-simulation-verification/

### Useful commands
- Stop the container
<pre>
sudo docker stop model-simulation-verification
</pre>

- Remove the container
<pre>
sudo docker rm model-simulation-verification
sudo docker ps -a
</pre>

- Remove the image
<pre>
sudo docker rmi model-simulation-verification
sudo docker images -a
</pre>

- Run the container shell for problem analysis
<pre>
sudo docker run -it -p 8080:8080 --name model-simulation-verification --rm model-simulation-verification bash

catalina.sh run&

exit
</pre>

### Notes
- If docker is running with parameter -p 8080:8080 the port 8080 of the host must be available