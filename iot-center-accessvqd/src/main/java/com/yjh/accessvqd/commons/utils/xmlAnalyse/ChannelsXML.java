package com.yjh.accessvqd.commons.utils.xmlAnalyse;

import com.yjh.accessvqd.module.diagnose.entity.Channel;
import com.yjh.accessvqd.module.diagnose.entity.DataServer;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChannelsXML {

    public static Channel unPackingXMl(String response) throws DocumentException {

        Channel channel=new Channel();

        Document doc=DocumentHelper.parseText(response);
        Element root=doc.getRootElement();
        List<Element> children=root.elements();
        for(Element child:children){
            if(child.getName().equals("id"))
                channel.setId(child.getText());
            if(child.getName().equals("checkFlag"))
                channel.setCheckFlag(child.getText());
            if(child.getName().equals("ip"))
                channel.setIp(child.getText());
            if(child.getName().equals("port"))
                channel.setPort(child.getText());
            if(child.getName().equals("chanIndex"))
                channel.setChanIndex(child.getText());
            if(child.getName().equals("signalPoint"))
                channel.setSignalPoint(child.getText());
            if(child.getName().equals("blurPoint"))
                channel.setBlurPoint(child.getText());
            if(child.getName().equals("contrastPoint"))
                channel.setContrastPoint(child.getText());
            if(child.getName().equals("brightPoint"))
                channel.setBrightPoint(child.getText());
            if(child.getName().equals("darkPoint"))
                channel.setDarkPoint(child.getText());
            if(child.getName().equals("chromaPoint"))
                channel.setChromaPoint(child.getText());
            if(child.getName().equals("monoPoint"))
                channel.setMonoPoint(child.getText());
            if(child.getName().equals("noisePoint"))
                channel.setNoisePoint(child.getText());
            if(child.getName().equals("streakPoint"))
                channel.setStreakPoint(child.getText());
            if(child.getName().equals("freezePoint"))
                channel.setFreezePoint(child.getText());
            if(child.getName().equals("shakePoint"))
                channel.setShakePoint(child.getText());
            if(child.getName().equals("flashPoint"))
                channel.setFlashPoint(child.getText());
            if(child.getName().equals("scenePoint"))
                channel.setScenePoint(child.getText());
            if(child.getName().equals("coverPoint"))
                channel.setCoverPoint(child.getText());
            if(child.getName().equals("ptzPoint"))
                channel.setPtzPoint(child.getText());
            if(child.getName().equals("streamType"))
                channel.setStreamType(child.getText());
            if(child.getName().equals("protocol"))
                channel.setProtocol(child.getText());
            if(child.getName().equals("devType"))
                channel.setDevType(child.getText());
            if(child.getName().equals("devBrand"))
                channel.setDevBrand(child.getText());
        }
        return channel;


    }


    public static List<Channel> unPackingXMlToList(String response) throws DocumentException{
        List<Channel> channels=new ArrayList<>();
        Document doc=DocumentHelper.parseText(response);
        Element root=doc.getRootElement();
        List<Element> supers=root.elements();
        for(Element s1:supers){
            Channel channel=new Channel();
            List<Element> children=s1.elements();
            for(Element child:children){
                if(child.getName().equals("id"))
                    channel.setId(child.getText());
                if(child.getName().equals("checkFlag"))
                    channel.setCheckFlag(child.getText());
                if(child.getName().equals("ip"))
                    channel.setIp(child.getText());
                if(child.getName().equals("port"))
                    channel.setPort(child.getText());
                if(child.getName().equals("chanIndex"))
                    channel.setChanIndex(child.getText());
                if(child.getName().equals("userName"))
                    channel.setUserName(child.getText());
                if(child.getName().equals("userPwd"))
                    channel.setUserPwd(child.getText());
                if(child.getName().equals("signalPoint"))
                    channel.setSignalPoint(child.getText());
                if(child.getName().equals("blurPoint"))
                    channel.setBlurPoint(child.getText());
                if(child.getName().equals("contrastPoint"))
                    channel.setContrastPoint(child.getText());
                if(child.getName().equals("brightPoint"))
                    channel.setBrightPoint(child.getText());
                if(child.getName().equals("darkPoint"))
                    channel.setDarkPoint(child.getText());
                if(child.getName().equals("chromaPoint"))
                    channel.setChromaPoint(child.getText());
                if(child.getName().equals("monoPoint"))
                    channel.setMonoPoint(child.getText());
                if(child.getName().equals("noisePoint"))
                    channel.setNoisePoint(child.getText());
                if(child.getName().equals("streakPoint"))
                    channel.setStreakPoint(child.getText());
                if(child.getName().equals("freezePoint"))
                    channel.setFreezePoint(child.getText());
                if(child.getName().equals("shakePoint"))
                    channel.setShakePoint(child.getText());
                if(child.getName().equals("flashPoint"))
                    channel.setFlashPoint(child.getText());
                if(child.getName().equals("scenePoint"))
                    channel.setScenePoint(child.getText());
                if(child.getName().equals("coverPoint"))
                    channel.setCoverPoint(child.getText());
                if(child.getName().equals("ptzPoint"))
                    channel.setPtzPoint(child.getText());
                if(child.getName().equals("streamType"))
                    channel.setStreamType(child.getText());
                if(child.getName().equals("protocol"))
                    channel.setProtocol(child.getText());
                if(child.getName().equals("devType"))
                    channel.setDevType(child.getText());
                if(child.getName().equals("devBrand"))
                    channel.setDevBrand(child.getText());
            }
            channels.add(channel);
        }
        return channels;
    }




    public static String generateChannelXML(Channel channel){
        Document document= DocumentHelper.createDocument();
        Element root=document.addElement("Channel");
        Element childNode1=root.addElement("id");
        childNode1.setText(channel.getId());
        Element childNode2=root.addElement("checkFlag");
        childNode2.setText(channel.getCheckFlag());
        Element childNode3=root.addElement("ip");
        childNode3.setText(channel.getIp());
        Element childNode4=root.addElement("port");
        childNode4.setText(channel.getPort());
        Element childNode5=root.addElement("chanIndex");
        childNode5.setText(channel.getChanIndex());
        Element childNode6=root.addElement("userName");
        childNode6.setText(channel.getUserName());
        Element childNode7=root.addElement("userPwd");
        childNode7.setText(channel.getUserPwd());
        Element childNode8=root.addElement("signalPoint");
        childNode8.setText(channel.getSignalPoint());
        Element childNode9=root.addElement("blurPoint");
        childNode9.setText(channel.getBlurPoint());
        if(Objects.nonNull(channel.getContrastPoint())){
            Element childNode10=root.addElement("contrastPoint");
            childNode10.setText(channel.getContrastPoint());
        }
        if(Objects.nonNull(channel.getBrightPoint())){
            Element childNode11=root.addElement("brightPoint");
            childNode11.setText(channel.getBrightPoint());
        }
        if(Objects.nonNull(channel.getDarkPoint())){
            Element childNode12=root.addElement("darkPoint");
            childNode12.setText(channel.getDarkPoint());
        }
        Element childNode13=root.addElement("chromaPoint");
        childNode13.setText(channel.getChromaPoint());
        if(Objects.nonNull(channel.getMonoPoint())){
            Element childNode14=root.addElement("monoPoint");
            childNode14.setText(channel.getMonoPoint());
        }
        Element childNode15=root.addElement("noisePoint");
        childNode15.setText(channel.getNoisePoint());
        Element childNode16=root.addElement("streakPoint");
        childNode16.setText(channel.getStreakPoint());
        Element childNode17=root.addElement("freezePoint");
        childNode17.setText(channel.getFreezePoint());
        if(Objects.nonNull(channel.getShakePoint())){
            Element childNode18=root.addElement("shakePoint");
            childNode18.setText(channel.getShakePoint());
        }
        if(Objects.nonNull(channel.getFlashPoint())){
            Element childNode19=root.addElement("flashPoint");
            childNode19.setText(channel.getFlashPoint());
        }
        if(Objects.nonNull(channel.getScenePoint())){
            Element childNode20=root.addElement("scenePoint");
            childNode20.setText(channel.getScenePoint());
        }

        if(Objects.nonNull(channel.getCoverPoint())){
            Element childNode21=root.addElement("coverPoint");
            childNode21.setText(channel.getCoverPoint());
        }
        Element childNode22=root.addElement("ptzPoint");
        childNode22.setText(channel.getPtzPoint());
        Element childNode23=root.addElement("streamType");
        childNode23.setText(channel.getStreamType());
        Element childNode24=root.addElement("protocol");
        childNode24.setText(channel.getProtocol());
        Element childNode25=root.addElement("devType");
        childNode25.setText(channel.getDevType());
        Element childNode26=root.addElement("devBrand");
        childNode26.setText(channel.getDevBrand());


        String xmlString=document.asXML();
        return xmlString;

    }
}
