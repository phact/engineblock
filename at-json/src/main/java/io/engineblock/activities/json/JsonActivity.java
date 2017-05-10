package io.engineblock.activities.json;

import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import io.engineblock.activities.json.statements.*;
import io.engineblock.activityapi.Activity;
import io.engineblock.activityimpl.ActivityDef;
import io.engineblock.activityimpl.SimpleActivity;
import io.engineblock.metrics.ActivityMetrics;
import io.engineblock.util.StrInterpolater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


public class JsonActivity extends SimpleActivity implements Activity{
    private final static Logger logger = LoggerFactory.getLogger(JsonActivity.class);
    private final FileStmtDocList stmtDocList;
    private ReadyFileStatementsTemplate readyFileStatementsTemplate;
    private String filename;

    private JsonGenerator generator;
    private Timer jsonWriteTimer;

    public JsonActivity(ActivityDef activityDef) {
        super(activityDef);
        StrInterpolater interp = new StrInterpolater(activityDef);

        String yaml_loc = activityDef.getParams().getOptionalString("yaml").orElse("default");
        this.filename = activityDef.getParams().getOptionalString("filename").orElse("out.json");

        YamlFileStatementLoader yamlLoader = new YamlFileStatementLoader(interp);
        stmtDocList = yamlLoader.load(yaml_loc, "activities");
    }

    @Override
    public void initActivity(){

        logger.debug("initializing activity: " + activityDef.getAlias());
        readyFileStatementsTemplate = createReadyFileStatementsTemplate();
        jsonWriteTimer = ActivityMetrics.timer(activityDef, "write");

        try {
            JsonFactory factory = new JsonFactory();
            generator = factory.createGenerator(new FileOutputStream(this.filename), JsonEncoding.UTF8);
            generator.setPrettyPrinter(new MinimalPrettyPrinter(""));

        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    public ReadyFileStatementsTemplate getReadyFileStatements() {
        return readyFileStatementsTemplate;
    }

    public Timer getJsonWriteTimer() { return jsonWriteTimer; }

    private ReadyFileStatementsTemplate createReadyFileStatementsTemplate() {
        ReadyFileStatementsTemplate readyFileStatements = new ReadyFileStatementsTemplate();
        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("");
        List<FileStmtDoc> matchingStmtDocs = stmtDocList.getMatching(tagfilter);

        for (FileStmtDoc doc : matchingStmtDocs) {
            for (FileStmtBlock section : doc.getAllBlocks()) {
                Map<String, String> bindings = section.getBindings();
                int indexer = 0;
                for (String stmt : section.getStatements()) {
                    String name = section.getName() + "-" + indexer++;
                    ReadyFileStatementTemplate t = new ReadyFileStatementTemplate(name, stmt,bindings);
                    readyFileStatements.addTemplate(t);
                }
            }
        }

        if (getActivityDef().getCycleCount() == 0) {
            logger.debug("Adjusting cycle count for " + activityDef.getAlias() + " to " +
                    readyFileStatements.size());
            getActivityDef().setCycles(String.valueOf(readyFileStatements.size()));
        }

        return readyFileStatements;
    }

    public synchronized void writeObject(List<String> objectNames, Object[] objectValues)
    {
        try {
            generator.writeStartObject();

            for(int i = 0; i < objectNames.size(); i++) {
                generator.writeStringField(objectNames.get(i), objectValues[i].toString());
            }

            generator.writeEndObject();
            generator.writeRaw("\n");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdownActivity() {
        try {
            generator.close();

        } catch(IOException e) {
            e.printStackTrace();
        }

    }
}