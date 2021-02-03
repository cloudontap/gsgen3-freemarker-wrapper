package io.hamlet.freemarkerwrapper.files.methods.tree;

import freemarker.core.Environment;
import freemarker.template.*;
import io.hamlet.freemarkerwrapper.RunFreeMarkerException;
import io.hamlet.freemarkerwrapper.files.adapters.JsonStringAdapter;
import io.hamlet.freemarkerwrapper.files.meta.LayerMeta;
import io.hamlet.freemarkerwrapper.files.processors.LayerProcessor;
import io.hamlet.freemarkerwrapper.utils.FreemarkerUtil;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class GetLayerTreeMethod {

    protected LayerMeta meta;
    protected TemplateHashModelEx options;
    protected LayerProcessor layerProcessor;

    public void parseArguments(List args) throws TemplateModelException {
        if (args.size() != 2) {
            throw new TemplateModelException("Wrong arguments");
        }
        String startingPath = FreemarkerUtil.getOptionStringValue(args.get(0));

        options = (TemplateHashModelEx)args.get(1);
        TemplateModelIterator iterator = options.keys().iterator();
        TemplateSequenceModel regexSequence = null;
        SimpleScalar regexScalar = null;
        boolean ignoreDotDirectories = Boolean.TRUE;
        boolean ignoreDotFiles = Boolean.TRUE;
        boolean addStartingWildcard = Boolean.TRUE;
        boolean addEndingWildcard = Boolean.TRUE;
        boolean stopAfterFirstMatch = Boolean.FALSE;
        boolean ignoreSubtreeAfterMatch = Boolean.FALSE;
        Number minDepth = null;
        Number maxDepth = null;
        boolean includeInformation = Boolean.FALSE;
        boolean caseSensitive = Boolean.FALSE;
        String filenameGlob = "*";

        while (iterator.hasNext()){
            TemplateModel keyModel = iterator.next();
            String key = keyModel.toString();
            Object keyObj = options.get(key);
            if ("Regex".equalsIgnoreCase(key)){
                if(keyObj instanceof TemplateSequenceModel)
                    regexSequence = (TemplateSequenceModel)keyObj;
                else if(keyObj instanceof SimpleScalar)
                    regexScalar = (SimpleScalar)keyObj;
            } else if ("IgnoreDotDirectories".equalsIgnoreCase(key)){
                ignoreDotDirectories = ((TemplateBooleanModel) keyObj).getAsBoolean();
            } else if ("IgnoreDotFiles".equalsIgnoreCase(key)){
                ignoreDotFiles = ((TemplateBooleanModel) keyObj).getAsBoolean();
            } else if ("AddStartingWildcard".equalsIgnoreCase(key)){
                addStartingWildcard = ((TemplateBooleanModel) keyObj).getAsBoolean();
            } else if ("AddEndingWildcard".equalsIgnoreCase(key)){
                addEndingWildcard = ((TemplateBooleanModel) keyObj).getAsBoolean();
            } else if ("StopAfterFirstMatch".equalsIgnoreCase(key)){
                stopAfterFirstMatch = ((TemplateBooleanModel) keyObj).getAsBoolean();
            } else if ("IgnoreSubtreeAfterMatch".equalsIgnoreCase(key)){
                ignoreSubtreeAfterMatch = ((TemplateBooleanModel) keyObj).getAsBoolean();
            } else if ("MinDepth".equalsIgnoreCase(key)){
                minDepth = ((TemplateNumberModel) keyObj).getAsNumber();
            } else if ("MaxDepth".equalsIgnoreCase(key)){
                maxDepth = ((TemplateNumberModel) keyObj).getAsNumber();
            } else if (meta.getIncludeInformationOptionName().equalsIgnoreCase(key)) {
                includeInformation = ((TemplateBooleanModel) keyObj).getAsBoolean();
            } else if ("CaseSensitive".equalsIgnoreCase(key)){
                caseSensitive = ((TemplateBooleanModel) keyObj).getAsBoolean();
            } else if ("FilenameGlob".equalsIgnoreCase(key)){
                filenameGlob = FreemarkerUtil.getOptionStringValue(keyObj);
            }
        }
        List<String> regexList = new ArrayList<>();
        if(regexSequence == null || regexSequence.size() == 0){
            if(regexScalar == null) {
                regexList.add("^.*$");
            } else {
                regexList.add(regexScalar.getAsString());
            }
        } else {
            for (int i=0; i < regexSequence.size();i++){
                regexList.add(regexSequence.get(i).toString());
            }
        }
        meta.setStartingPath(startingPath);

        meta.setRegexList(regexList);
        meta.setIgnoreDotDirectories(ignoreDotDirectories);
        meta.setIgnoreDotFiles(ignoreDotFiles);
        meta.setIncludeInformation(includeInformation);
        meta.setAddStartingWildcard(addStartingWildcard);
        meta.setAddEndingWildcard(addEndingWildcard);
        meta.setStopAfterFirstMatch(stopAfterFirstMatch);
        meta.setIgnoreSubtreeAfterMatch(ignoreSubtreeAfterMatch);
        if (minDepth!=null){
            meta.setMinDepth(minDepth.intValue());
        }
        if (maxDepth!=null){
            meta.setMaxDepth(maxDepth.intValue());
        }
        meta.setCaseSensitive(caseSensitive);
        meta.setFilenameGlob(filenameGlob);
    }

    public TemplateModel process() {
        layerProcessor.setConfiguration(Environment.getCurrentEnvironment().getConfiguration());
        Set<JsonObject> result = null;
        try {
            result = layerProcessor.getLayerTree(meta);
        } catch (RunFreeMarkerException e) {
            e.printStackTrace();
        }
        return new SimpleSequence(result, Environment.getCurrentEnvironment().getConfiguration().getObjectWrapper());
    }
}
