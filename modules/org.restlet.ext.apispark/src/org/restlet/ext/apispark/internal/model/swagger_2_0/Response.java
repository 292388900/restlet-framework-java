package org.restlet.ext.apispark.internal.model.swagger_2_0;

import org.restlet.ext.apispark.internal.model.swagger_2_0.Model;
import org.restlet.ext.apispark.internal.model.swagger_2_0.properties.Property;

import java.util.*;

public class Response {
  String description;
  Property schema;
  Map<String, String> examples;

  public Response schema(Property property) {
    this.setSchema(property);
    return this;
  }
  public Response description(String description) {
    this.setDescription(description);
    return this;
  }
  public Response example(String type, String example) {
    if(examples == null) {
      examples = new HashMap<String, String>();
    }
    examples.put(type, example);
    return this;
  }

  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  public Property getSchema() {
    return schema;
  }
  public void setSchema(Property schema) {
    this.schema = schema;
  }

  public Map<String, String> getExamples() {
    return this.examples;
  }
  public void setExamples(Map<String, String> examples) {
    this.examples = examples;
  }
}
