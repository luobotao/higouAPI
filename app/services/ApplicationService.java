package services;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.AdBanner;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;

import play.Logger;
import repositories.ApplicationRepository;

@Named
@Singleton
public class ApplicationService {
    private static final Logger.ALogger LOGGER = Logger.of(ApplicationService.class);

    @Inject
    private ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public List<AdBanner> getAdBanner() {
    	Sort sort = new Sort(Direction.DESC, "rank","id");
        return applicationRepository.findAll(sort);
    }


    

}
