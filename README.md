# awesome-hackathon
aws bedrock playground


# awesome-hackathon Service (awesome-hackathon)
awesome-hackathon Service.


### Running `awesome-hackathon` service from terminal
The project can be run in development mode by issuing the following command from project root directory.

The sbt-revolver plugin can also be used to fork the process to run the project.

```shell
sbt
```
* Navigate to `awesome-hackathon-svc` folder

```shell
sbt:asset-svc>project awesome-hackathon-svc
```

```shell
reStart --- -Dconfig.resource=local1.conf
reStop
```

### Executing `awesome-hackathon`  unittests
Executing the unittests is as simple as:
```shell
sbt awesome-hackathon/test
```

