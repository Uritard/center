package com.yjh.video.core.gb28181.transmit.event.response.impl;

import com.yjh.video.core.config.SipConfig;
import com.yjh.video.core.gb28181.session.VideoStreamSessionManager;
import com.yjh.video.core.gb28181.transmit.SIPProcessorObserver;
import com.yjh.video.core.gb28181.transmit.cmd.ISIPCommander;
import com.yjh.video.core.gb28181.transmit.cmd.SIPRequestHeaderProvider;
import com.yjh.video.core.gb28181.transmit.event.response.SIPResponseProcessorAbstract;
import com.yjh.video.core.service.IDeviceEventService;
import gov.nist.javax.sip.ResponseEventExt;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.message.SIPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.address.SipURI;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;


/**
 * @description: 处理INVITE响应
 * @author: panlinlin
 * @date: 2021年11月5日 16：40
 */
@Component
public class InviteResponseProcessor extends SIPResponseProcessorAbstract {

	private final static Logger logger = LoggerFactory.getLogger(InviteResponseProcessor.class);
	private final String method = "INVITE";

	@Autowired
	private VideoStreamSessionManager streamSession;

	@Autowired
	private SIPProcessorObserver sipProcessorObserver;

	@Autowired
	private SipConfig sipConfig;

	@Autowired
	private SipFactory sipFactory;

	@Autowired
	private ISIPCommander commander;

	@Autowired
	private IDeviceEventService deviceService;

	@Autowired
	private SIPRequestHeaderProvider headerProvider;

	@Autowired
	@Qualifier(value="udpSipProvider")
	private SipProviderImpl udpSipProvider;


	@Override
	public void afterPropertiesSet() throws Exception {
		// 添加消息处理的订阅
		sipProcessorObserver.addResponseProcessor(method, this);
	}



	/**
	 * 处理invite响应
	 * 
	 * @param evt 响应消息
	 * @throws ParseException
	 */
	@Override
	public void process(ResponseEvent evt ){
		try {

			SIPResponse response = (SIPResponse)evt.getResponse();
			int statusCode = response.getStatusCode();
			// trying不会回复
			if (statusCode == Response.TRYING) {
			}
			// 成功响应
			// 下发ack
			if (statusCode == Response.OK) {
				ResponseEventExt event = (ResponseEventExt)evt;

				String contentString = new String(response.getRawContent());
				// jainSip不支持y=字段， 移除以解析。
				int ssrcIndex = contentString.indexOf("y=");
				// 检查是否有y字段
				SessionDescription sdp;
				if (ssrcIndex >= 0) {
					//ssrc规定长度为10字节，不取余下长度以避免后续还有“f=”字段
					String substring = contentString.substring(0, contentString.indexOf("y="));
					sdp = SdpFactory.getInstance().createSessionDescription(substring);
				} else {
					sdp = SdpFactory.getInstance().createSessionDescription(contentString);
				}
				SipURI requestUri = sipFactory.createAddressFactory().createSipURI(sdp.getOrigin().getUsername(), event.getRemoteIpAddress() + ":" + event.getRemotePort());
				Request reqAck = headerProvider.createAckRequest(requestUri, response);

				logger.info("[回复ack] {}-> {}:{} ", sdp.getOrigin().getUsername(), event.getRemoteIpAddress(), event.getRemotePort());
				commander.transmitRequest(response.getTopmostViaHeader().getTransport(), reqAck, null, null);

			}
		} catch (InvalidArgumentException | ParseException | SipException | SdpParseException e) {
			logger.info("[点播回复ACK]，异常：", e );
		}
	}

}
