package com.att.report;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.log.Log;

public class XmlReportParser {
    private Logger logger = Log.getSlientLogger("XmlReportParser");
    private List<String> xmlFileList = new ArrayList<String>();
    private HashMap<String, UseCase> mUseCaseMap = new HashMap<String, UseCase>();
    
    public void addXmlFile(String xmlFile){
        xmlFileList.add(xmlFile);
    }
    
    public void addXmlFiles(String[] files){
        for(String s:files){
            addXmlFile(s);
        }
    }
    
    public void doParse(){
        for(int i=0;i<xmlFileList.size();i++){
            String filename = xmlFileList.get(i);
            logger.info("parse file:"+filename);
            parseOneFile(filename);
        }
    }
    
    public List<UseCase> getTotalUseCases(){
        List<UseCase> mUseCaseList = new ArrayList<UseCase>();
        for(UseCase uc:mUseCaseMap.values()){
            mUseCaseList.add(uc);
        }
        return mUseCaseList;
    }
    
    public List<UseCase> getPassedUseCases(){
        List<UseCase> mUseCaseList = new ArrayList<UseCase>();
        for(UseCase uc:mUseCaseMap.values()){
            if(uc.isPassed())
                mUseCaseList.add(uc);
        }
        return mUseCaseList;
    } 
    
    public List<UseCase> getFailedUseCases(){
        List<UseCase> mUseCaseList = new ArrayList<UseCase>();
        for(UseCase uc:mUseCaseMap.values()){
            if(!uc.isPassed())
                mUseCaseList.add(uc);
        }
        return mUseCaseList;
    }  
    
   // public void add
    
    public void parseOneFile(String filename){
        File file = new File(filename);
        SAXReader reader = new SAXReader();
        Document document;
        try {
            
            if(!file.exists()){
                logger.severe("文件不存在:"+filename);
                return;
            }
            
            if(file.length()<10){
                logger.severe("文件格式不正确:"+filename);
                return;
            }
            
            document = reader.read(file);
            Element root = document.getRootElement();

            Element testSuitsElement = root.element("testsuite");
            if (testSuitsElement == null) {
                return;
            }
            
            for (Iterator i = testSuitsElement.elementIterator("testcase"); i
                    .hasNext();) {
                Element foo = (Element) i.next();
                UseCase uc = new UseCase(foo);
                String test = uc.getAttr("classname")+"."+uc.getAttr("name");
                if(mUseCaseMap.get(test)==null){
                    mUseCaseMap.put(test, uc);
                    continue;
                }
                
                //如果本身没有通过,  那另一个不管是pass还是不pass, 都不需要替换了
                if(!uc.isPassed()) continue;
                
                mUseCaseMap.put(test, uc);
//                // printElementAttr(listener, foo);
//                mUseCaseList.add();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Exception e:"+e.getMessage());
        }

    }
    
    public static void main(String[] args){
        File workspace = new File("testreport/015d18846938120f");
        if(!workspace.exists()) {
            //logger.severe("testreport/"+serial+" 目录不存在,这是什么问题:");
            return;
        }
        
        String[] filelist = workspace.list(new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name) {
                if(name.startsWith("testresult") && name.endsWith(".xml")) return true;
                return false;
            }
        });
        
        XmlReportParser parser=new XmlReportParser();
        for(String s:filelist){
            parser.addXmlFile("testreport/015d18846938120f/"+s);
        }
        
        parser.doParse();
        List<UseCase> useCases = parser.getTotalUseCases();
        System.out.println("total:"+useCases.size());
        List<UseCase> passed = parser.getPassedUseCases();
        System.out.println("passed:"+passed.size());
        List<UseCase> failed = parser.getFailedUseCases();
        System.out.println("failed:"+failed.size());
    }
}
