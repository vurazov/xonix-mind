package com.lineate.xonix.mind.service;

import java.util.List;
import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.exception.ServiceException;
import com.lineate.xonix.mind.model.Bot;
import com.lineate.xonix.mind.model.BuildParam;

public interface BotService {

    String getLocalHeadHashByUrl(String url);

    List<Bot> getBots(List<String> urls, BuildParam buildParam) throws ServiceException;

    BotDb save(BotDb botDb);

    List<Bot> mapBots(List<BotDb> botDbs) throws ServiceException;
}
