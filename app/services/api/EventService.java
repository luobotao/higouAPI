package services.api;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Event;
import models.EventLabel;
import models.EventParam;
import play.Logger;
import repositories.ErpAddressRepository;
import repositories.EventLabelRepository;
import repositories.EventParamRepository;
import repositories.EventRepository;

/**
 * 频道相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class EventService {

    private static final Logger.ALogger logger = Logger.of(EventService.class);
    @Inject
    private EventRepository eventRepository;
    @Inject
    private EventLabelRepository eventLabelRepository;
    @Inject
    private EventParamRepository eventParamRepository;
    
    public Event findEventByEventName(String eventName) {
    	return eventRepository.findByEventName(eventName);
    }

	public Event save(Event event) {
		return eventRepository.save(event);
	}
   
	public EventLabel findByEventidAndLabel(Event eventid,String label) {
		return eventLabelRepository.findByEventidAndLabel(eventid,label);
	}
	public EventLabel save(EventLabel eventLabel) {
		return eventLabelRepository.save(eventLabel);
	}

	public EventParam save(EventParam eventParam) {
		return eventParamRepository.save(eventParam);
		
	}
}
