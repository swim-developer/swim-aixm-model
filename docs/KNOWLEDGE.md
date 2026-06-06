# swim-aixm-model — Knowledge Base


## What This Is

**JAXB data model for AIXM 5.1.1 + GML 3.2.1.** Used by `swim-digital-notam-consumer`, `swim-digital-notam-provider`, and their validators for XML marshalling/unmarshalling of DNOTAM events.

Generated via XJC (JAXB) from the official AIXM 5.1.1 XSD schemas. Do not hand-edit generated classes.

## Usage

```xml
<dependency>
    <groupId>com.github.swim-developer</groupId>
    <artifactId>swim-aixm-model</artifactId>
</dependency>
```

## Build

```bash
mvn clean install -DskipTests
```

## Key Packages

| Package | Content |
|---------|---------|
| `aero.aixm.schema._5_1` | AIXM feature types (RunwayDirection, Airspace, etc.) |
| `net.opengis.gml._3_2` | GML geometry types |
| `aero.fixm.schema.event` | DNOTAM Event scenario types |
