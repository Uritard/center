package com.yjh.accessvqd.commons.utils.xmlAnalyse;

import com.yjh.accessvqd.module.diagnose.entity.Plans;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlansXML {

    //解析PlanList
    public static List<Plans> unPackingXML(String response) throws DocumentException {
        List<Plans> plansList = new ArrayList<>();
        Document doc = DocumentHelper.parseText(response);
        Element root = doc.getRootElement();
        List<Element> supers = root.elements();
        for (Element childs : supers) {
            Plans plans = new Plans();
            List<Element> childNodes = childs.elements();
            for (Element child : childNodes) {
                if (child.getName().equals("id"))
                    plans.setDiagnosePlanId(child.getText());
                if (child.getName().equals("checkFlag"))
                    plans.setCheckFlag(child.getText());
                if (child.getName().equals("period"))
                    plans.setPeriod(child.getText());
                if (child.getName().equals("repeat"))
                    plans.setRepeat(child.getText());
                if(child.getName().equals("week")){
                    List<Element> weeks=child.elements();
                    for(Element week:weeks){
                        if(week.getName().equals("mon")){
                            Element diagnoseTime=week.element("DiagnoseTime");
                            Element time=diagnoseTime.element("Time");
                            if(Objects.nonNull(time)){
                                Element start=time.element("StartTime");
                                plans.setStartTime1(start.getText());
                                Element end=time.element("EndTime");
                                plans.setEndTime1(end.getText());
                            }

                        }
                        if(week.getName().equals("tues")){
                            Element diagnoseTime=week.element("DiagnoseTime");
                            Element time=diagnoseTime.element("Time");
                            if(Objects.nonNull(time)) {
                                Element start = time.element("StartTime");
                                plans.setStartTime1(start.getText());
                                Element end = time.element("EndTime");
                                plans.setEndTime1(end.getText());
                            }

                        }
                        if(week.getName().equals("wed")){
                            Element diagnoseTime=week.element("DiagnoseTime");
                            Element time=diagnoseTime.element("Time");
                            if(Objects.nonNull(time)) {
                                Element start = time.element("StartTime");
                                plans.setStartTime1(start.getText());
                                Element end = time.element("EndTime");
                                plans.setEndTime1(end.getText());
                            }

                        }
                        if(week.getName().equals("thur")){
                            Element diagnoseTime=week.element("DiagnoseTime");
                            Element time=diagnoseTime.element("Time");
                            if(Objects.nonNull(time)) {
                                Element start = time.element("StartTime");
                                plans.setStartTime1(start.getText());
                                Element end = time.element("EndTime");
                                plans.setEndTime1(end.getText());
                            }

                        }
                        if(week.getName().equals("fri")){
                            Element diagnoseTime=week.element("DiagnoseTime");
                            Element time=diagnoseTime.element("Time");
                            if(Objects.nonNull(time)) {
                                Element start = time.element("StartTime");
                                plans.setStartTime1(start.getText());
                                Element end = time.element("EndTime");
                                plans.setEndTime1(end.getText());
                            }

                        }
                        if(week.getName().equals("sat")){
                            Element diagnoseTime=week.element("DiagnoseTime");
                            Element time=diagnoseTime.element("Time");
                            if(Objects.nonNull(time)) {
                                Element start = time.element("StartTime");
                                plans.setStartTime1(start.getText());
                                Element end = time.element("EndTime");
                                plans.setEndTime1(end.getText());
                            }

                        }
                        if(week.getName().equals("sun")){
                            Element diagnoseTime=week.element("DiagnoseTime");
                            Element time=diagnoseTime.element("Time");
                            if(Objects.nonNull(time)) {
                                Element start = time.element("StartTime");
                                plans.setStartTime1(start.getText());
                                Element end = time.element("EndTime");
                                plans.setEndTime1(end.getText());
                            }

                        }
                    }
                }
                if (child.getName().equals("CheckItems")) {
                    List<Element> lowers = child.elements();
                    for (Element low : lowers) {
                        if (low.getName().equals("signal"))
                            plans.setSignal(low.getText());
                        if (low.getName().equals("blur"))
                            plans.setBlur(low.getText());
                        if (low.getName().equals("contrast"))
                            plans.setContrast(low.getText());
                        if (low.getName().equals("bright"))
                            plans.setBright(low.getText());
                        if (low.getName().equals("dark"))
                            plans.setDark(low.getText());
                        if (low.getName().equals("chroma"))
                            plans.setChroma(low.getText());
                        if (low.getName().equals("mono"))
                            plans.setMono(low.getText());
                        if (low.getName().equals("noise"))
                            plans.setNoise(low.getText());
                        if (low.getName().equals("streak"))
                            plans.setStreak(low.getText());
                        if (low.getName().equals("freeze"))
                            plans.setFreeze(low.getText());
                        if (low.getName().equals("shake"))
                            plans.setShake(low.getText());
                        if (low.getName().equals("flash"))
                            plans.setFlash(low.getText());
                        if (low.getName().equals("scene"))
                            plans.setScene(low.getText());
                        if (low.getName().equals("cover"))
                            plans.setCover(low.getText());
                        if (low.getName().equals("ptz"))
                            plans.setPtz(low.getText());
                    }
                }
                if (child.getName().equals("TaskList")) {
                    List<Element> lowers = child.elements();
                    List<String> taskIds = new ArrayList<>();
                    for (Element low : lowers) {
                        if (low.getName().equals("taskID"))
                            taskIds.add(low.getText());
                    }
                    plans.setTaskList(taskIds);
                }

            }
            plansList.add(plans);

        }

        return plansList;
    }


    //解析PlanInfo
    public static Plans unpackingXMLByPlanId(String response) throws DocumentException {
        Plans plans=new Plans();
        Document doc=DocumentHelper.parseText(response);
        Element root=doc.getRootElement();
        List<Element> childNodes=root.elements();
        for (Element child : childNodes) {
            if (child.getName().equals("id"))
                plans.setDiagnosePlanId(child.getText());
            if (child.getName().equals("checkFlag"))
                plans.setCheckFlag(child.getText());
            if (child.getName().equals("period"))
                plans.setPeriod(child.getText());
            if (child.getName().equals("repeat"))
                plans.setRepeat(child.getText());
            if(child.getName().equals("week")){
                List<Element> weeks=child.elements();
                for(Element week:weeks){
                    if(week.getName().equals("mon")){
                        Element diagnoseTime=week.element("DiagnoseTime");
                        Element time=diagnoseTime.element("Time");
                        if(Objects.nonNull(time)){
                            Element start=time.element("StartTime");
                            plans.setStartTime1(start.getText());
                            Element end=time.element("EndTime");
                            plans.setEndTime1(end.getText());
                        }

                    }
                    if(week.getName().equals("tues")){
                        Element diagnoseTime=week.element("DiagnoseTime");
                        Element time=diagnoseTime.element("Time");
                        if(Objects.nonNull(time)) {
                            Element start = time.element("StartTime");
                            plans.setStartTime1(start.getText());
                            Element end = time.element("EndTime");
                            plans.setEndTime1(end.getText());
                        }

                    }
                    if(week.getName().equals("wed")){
                        Element diagnoseTime=week.element("DiagnoseTime");
                        Element time=diagnoseTime.element("Time");
                        if(Objects.nonNull(time)) {
                            Element start = time.element("StartTime");
                            plans.setStartTime1(start.getText());
                            Element end = time.element("EndTime");
                            plans.setEndTime1(end.getText());
                        }

                    }
                    if(week.getName().equals("thur")){
                        Element diagnoseTime=week.element("DiagnoseTime");
                        Element time=diagnoseTime.element("Time");
                        if(Objects.nonNull(time)) {
                            Element start = time.element("StartTime");
                            plans.setStartTime1(start.getText());
                            Element end = time.element("EndTime");
                            plans.setEndTime1(end.getText());
                        }

                    }
                    if(week.getName().equals("fri")){
                        Element diagnoseTime=week.element("DiagnoseTime");
                        Element time=diagnoseTime.element("Time");
                        if(Objects.nonNull(time)) {
                            Element start = time.element("StartTime");
                            plans.setStartTime1(start.getText());
                            Element end = time.element("EndTime");
                            plans.setEndTime1(end.getText());
                        }

                    }
                    if(week.getName().equals("sat")){
                        Element diagnoseTime=week.element("DiagnoseTime");
                        Element time=diagnoseTime.element("Time");
                        if(Objects.nonNull(time)) {
                            Element start = time.element("StartTime");
                            plans.setStartTime1(start.getText());
                            Element end = time.element("EndTime");
                            plans.setEndTime1(end.getText());
                        }

                    }
                    if(week.getName().equals("sun")){
                        Element diagnoseTime=week.element("DiagnoseTime");
                        Element time=diagnoseTime.element("Time");
                        if(Objects.nonNull(time)) {
                            Element start = time.element("StartTime");
                            plans.setStartTime1(start.getText());
                            Element end = time.element("EndTime");
                            plans.setEndTime1(end.getText());
                        }

                    }
                }
            }
            if (child.getName().equals("CheckItems")) {
                List<Element> lowers = child.elements();
                for (Element low : lowers) {
                    if (low.getName().equals("signal"))
                        plans.setSignal(low.getText());
                    if (low.getName().equals("blur"))
                        plans.setBlur(low.getText());
                    if (low.getName().equals("contrast"))
                        plans.setContrast(low.getText());
                    if (low.getName().equals("bright"))
                        plans.setBright(low.getText());
                    if (low.getName().equals("dark"))
                        plans.setDark(low.getText());
                    if (low.getName().equals("chroma"))
                        plans.setChroma(low.getText());
                    if (low.getName().equals("mono"))
                        plans.setMono(low.getText());
                    if (low.getName().equals("noise"))
                        plans.setNoise(low.getText());
                    if (low.getName().equals("streak"))
                        plans.setStreak(low.getText());
                    if (low.getName().equals("freeze"))
                        plans.setFreeze(low.getText());
                    if (low.getName().equals("shake"))
                        plans.setShake(low.getText());
                    if (low.getName().equals("flash"))
                        plans.setFlash(low.getText());
                    if (low.getName().equals("scene"))
                        plans.setScene(low.getText());
                    if (low.getName().equals("cover"))
                        plans.setCover(low.getText());
                    if (low.getName().equals("ptz"))
                        plans.setPtz(low.getText());
                }
            }
            if (child.getName().equals("TaskList")) {
                List<Element> lowers = child.elements();
                List<String> taskIds = new ArrayList<>();
                for (Element low : lowers) {
                    if (low.getName().equals("taskID"))
                        taskIds.add(low.getText());
                }
                plans.setTaskList(taskIds);
            }

        }


        return plans;
    }


    public static List<String> unpackingXmlTaskList(String response) throws DocumentException {
        List<String>taskList=new ArrayList<>();
        Document doc=DocumentHelper.parseText(response);
        Element root=doc.getRootElement();
        List<Element> childrenNode=root.elements();
        for(Element child:childrenNode){
            if(child.getName().equals("taskID"))
                taskList.add(child.getText());
        }
        return taskList;
    }
    public static String generatePlansXMl(Plans plans) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("PlanInfo");

        Element childNode1 = root.addElement("id");
        childNode1.setText(plans.getDiagnosePlanId());

        Element childNode2 = root.addElement("checkFlag");
        childNode2.setText(plans.getCheckFlag());

        Element childNode3 = root.addElement("period");
        childNode3.setText(plans.getPeriod());

        if (plans.getPeriod().equals("0")) {
            Element childNode4 = root.addElement("week");

            if (plans.getWeeks().size() != 0 && plans.getWeeks().contains("mon")) {
                Element childNode41 = childNode4.addElement("mon");
                Element childNode41A = childNode41.addElement("DiagnoseTime");
                Element childNode41AT = childNode41A.addElement("Time");
                Element childNode41ATs = childNode41AT.addElement("startTime");
                childNode41ATs.setText(plans.getStartTime1());
                Element childNode41ATe = childNode41AT.addElement("endTime");
                childNode41ATe.setText(plans.getEndTime1());
            }

            if (plans.getWeeks().size() != 0 && plans.getWeeks().contains("tues")) {
                Element childNode42 = childNode4.addElement("tues");
                Element childNode42A = childNode42.addElement("DiagnoseTime");
                Element childNode42AT = childNode42A.addElement("Time");
                Element childNode42ATs = childNode42AT.addElement("startTime");
                childNode42ATs.setText(plans.getStartTime1());
                Element childNode42ATe = childNode42AT.addElement("endTime");
                childNode42ATe.setText(plans.getEndTime1());
            }


            if (plans.getWeeks().size() != 0 && plans.getWeeks().contains("wed")) {
                Element childNode43 = childNode4.addElement("wed");
                Element childNode43A = childNode43.addElement("DiagnoseTime");
                Element childNode43AT = childNode43A.addElement("Time");
                Element childNode43ATs = childNode43AT.addElement("startTime");
                childNode43ATs.setText(plans.getStartTime1());
                Element childNode43ATe = childNode43AT.addElement("endTime");
                childNode43ATe.setText(plans.getEndTime1());
            }


            if (plans.getWeeks().size() != 0 && plans.getWeeks().contains("thur")) {
                Element childNode44 = childNode4.addElement("thur");
                Element childNode44A = childNode44.addElement("DiagnoseTime");
                Element childNode44AT = childNode44A.addElement("Time");
                Element childNode44ATs = childNode44AT.addElement("startTime");
                childNode44ATs.setText(plans.getStartTime1());
                Element childNode44ATe = childNode44AT.addElement("endTime");
                childNode44ATe.setText(plans.getEndTime1());
            }


            if (plans.getWeeks().size() != 0 && plans.getWeeks().contains("fri")) {
                Element childNode45 = childNode4.addElement("fri");
                Element childNode45A = childNode45.addElement("DiagnoseTime");
                Element childNode45AT = childNode45A.addElement("Time");
                Element childNode45ATs = childNode45AT.addElement("startTime");
                childNode45ATs.setText(plans.getStartTime1());
                Element childNode45ATe = childNode45AT.addElement("endTime");
                childNode45ATe.setText(plans.getEndTime1());
            }


            if (plans.getWeeks().size() != 0 && plans.getWeeks().contains("sat")) {
                Element childNode46 = childNode4.addElement("sat");
                Element childNode46A = childNode46.addElement("DiagnoseTime");
                Element childNode46AT = childNode46A.addElement("Time");
                Element childNode46ATs = childNode46AT.addElement("startTime");
                childNode46ATs.setText(plans.getStartTime1());
                Element childNode46ATe = childNode46AT.addElement("endTime");
                childNode46ATe.setText(plans.getEndTime1());
            }


            if (plans.getWeeks().size() != 0 && plans.getWeeks().contains("sun")) {
                Element childNode47 = childNode4.addElement("sun");
                Element childNode47A = childNode47.addElement("DiagnoseTime");
                Element childNode47AT = childNode47A.addElement("Time");
                Element childNode47ATs = childNode47AT.addElement("startTime");
                childNode47ATs.setText(plans.getStartTime1());
                Element childNode47ATe = childNode47AT.addElement("endTime");
                childNode47ATe.setText(plans.getEndTime1());
            }

        }

        if (Objects.nonNull(plans.getRepeat())) {
            Element childNode5 = root.addElement("repeat");
            childNode5.setText(plans.getRepeat());
        }

        Element childNode6 = root.addElement("CheckItems");

        Element childNode61 = childNode6.addElement("signal");
        childNode61.setText(plans.getSignal());

        Element childNode62 = childNode6.addElement("blur");
        childNode62.setText(plans.getBlur());

        Element childNode63 = childNode6.addElement("contrast");
        childNode63.setText(plans.getContrast());

        Element childNode64 = childNode6.addElement("bright");
        childNode64.setText(plans.getBright());

        Element childNode65 = childNode6.addElement("dark");
        childNode65.setText(plans.getDark());

        Element childNode66 = childNode6.addElement("chroma");
        childNode66.setText(plans.getChroma());

        Element childNode67 = childNode6.addElement("mono");
        childNode67.setText(plans.getMono());

        Element childNode68 = childNode6.addElement("noise");
        childNode68.setText(plans.getNoise());

        Element childNode69 = childNode6.addElement("streak");
        childNode69.setText(plans.getStreak());

        Element childNode610 = childNode6.addElement("freeze");
        childNode610.setText(plans.getFreeze());

        Element childNode611 = childNode6.addElement("shake");
        childNode611.setText(plans.getShake());

        Element childNodeB612 = childNode6.addElement("flash");
        childNodeB612.setText(plans.getFlash());

        Element childNode613 = childNode6.addElement("scene");
        childNode613.setText(plans.getScene());

        Element childNode614 = childNode6.addElement("cover");
        childNode614.setText(plans.getCover());

        Element childNode615 = childNode6.addElement("ptz");
        childNode615.setText(plans.getPtz());

        Element childNode7 = root.addElement("TaskList");
        for (String taskId : plans.getTaskList()) {
            childNode7.addElement("taskID").setText(taskId);
        }

        String xmlString = document.asXML();

        return xmlString;

    }
}
