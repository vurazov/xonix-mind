package com.lineate.xonix.mind.service;

import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.exception.ServiceException;
import com.lineate.xonix.mind.model.Bot;
import com.lineate.xonix.mind.model.BuildParam;
import com.lineate.xonix.mind.repositories.BotRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BotServiceImpl implements BotService {

    @Qualifier(value = "botRepositoriesProvider")
    @NonNull
    private final BotProvider botProvider;

    @NonNull
    private final BotRepository botRepository;

    @Autowired
    public BotServiceImpl(BotProvider botProvider, BotRepository botRepository) {
        this.botProvider = botProvider;
        this.botRepository = botRepository;
    }

    //TODO: Return message about failed bots

    @Override
    public BotDb save(BotDb botDb) {
        return botRepository.save(botDb);
    }

    @Override
    public List<Bot> mapBots(List<BotDb> botDbs) throws ServiceException {
        val bots = new ArrayList<Bot>();
        for (BotDb botDb : botDbs) {
            val o1 = convert(botDb.getSrcUrl());
            val o2 = o1.flatMap(botProvider::get);
            o2.ifPresent(bots::add);
        }
        return bots;
    }

    @Override
    public String getLocalHeadHashByUrl(String url) {
        Optional<URL> optUrl = convert(url);
        URL botUrl = optUrl.orElseThrow(() -> new ServiceException("Bot url is empty"));
        return botProvider.getLocalHeadHashByUrl(botUrl);
    }

    private Optional<URL> convert(String url) {
        try {
            return Optional.of(new URL(url));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    private List<URL> convert(List<String> urls) {
        return urls.stream().map(this::convert)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    @Override
    public List<Bot> getBots(List<String> botRepoUrls, BuildParam buildParam) throws ServiceException {
        if (botRepoUrls == null || botRepoUrls.isEmpty())
            return Collections.emptyList();

        List<URL> botUrls = convert(botRepoUrls);

        if (botUrls.isEmpty())
            return Collections.emptyList();

        return botProvider.retrieve(botUrls, buildParam);
    }
}
