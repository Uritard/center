package com.yjh.accessvqd.commons.utils.xmlAnalyse;

import com.yjh.accessvqd.module.diagnose.entity.ChanResult;
import io.swagger.models.auth.In;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.el.ELException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.SimpleFormatter;

public class ChanResultXML {

    public static ChanResult unPackingXMl(String xml) throws DocumentException, ParseException {

        ChanResult chanResult = new ChanResult();
        try {
            Document doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement();
            Element childNode1 = root.element("id");
            chanResult.setChannelId(childNode1.getText());
            Element childNode2 = root.element("ip");
            chanResult.setIp(childNode2.getText());
            Element childNode3 = root.element("chanIndex");
            chanResult.setChanIndex(childNode3.getText());
            Element childNode4 = root.element("width");
            Element childNode5 = root.element("height");
            Element childNode6 = root.element("checkTime");
            chanResult.setCheckTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(childNode6.getText()));
            Element childNode7 = root.element("videotype");
            Element childNode8 = root.element("result");
            chanResult.setChannelResult(Integer.valueOf(childNode8.getText()));
            Element childNode9 = root.element("resultOfSignal");
            chanResult.setSignalResult(childNode9.getText());
            Element childNode10 = root.element("resultOfBlur");
            chanResult.setBlurResult(childNode10.getText());
            Element childNode11 = root.element("resultOfContrast");
            chanResult.setContrastResult(childNode11.getText());
            Element childNode12 = root.element("resultOfBright");
            chanResult.setBrightResult(childNode12.getText());
            Element childNode13 = root.element("resultOfDark");
            chanResult.setDarkResult(childNode13.getText());
            Element childNode14 = root.element("resultOfChroma");
            chanResult.setChromaResult(childNode14.getText());
            Element childNode15 = root.element("resultOfMono");
            chanResult.setMonoResult(childNode15.getText());
            Element childNode16 = root.element("resultOfNoise");
            chanResult.setNoiseResult(childNode16.getText());
            Element childNode17 = root.element("resultOfStreak");
            chanResult.setStreakResult(childNode17.getText());
            Element childNode18 = root.element("resultOfFreeze");
            chanResult.setFreezeResult(childNode18.getText());
            Element childNode19 = root.element("resultOfShake");
            chanResult.setShakeResult(childNode19.getText());
            Element childNode20 = root.element("resultOfFlash");
            chanResult.setFlashResult(childNode20.getText());
            Element childNode21 = root.element("resultOfScene");
            chanResult.setSceneResult(childNode21.getText());
            Element childNode22 = root.element("resultOfCover");
            chanResult.setCoverResult(childNode22.getText());
            Element childNode23 = root.element("resultOfPTZ");
            chanResult.setPtzResult(childNode23.getText());
            Element childNode24=root.element("snapshotURL");
            chanResult.setSnapshotUrl(childNode24.getText());
            Element childNode25=root.element("width");
            chanResult.setWidth(childNode25.getText());
            Element childNode26=root.element("height");
            chanResult.setHeight(childNode26.getText());
        }catch (Exception e){
            e.printStackTrace();
        }


        return chanResult;


    }
}
