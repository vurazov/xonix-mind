package com.lineate.xonix.mind.service;

import com.lineate.xonix.mind.exception.ServiceException;
import com.lineate.xonix.mind.model.Bot;
import com.lineate.xonix.mind.model.BuildParam;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public interface BotProvider {
    List<Bot> retrieve(List<URL> botUrls, BuildParam buildParam) throws ServiceException;

    Optional<Bot> get(URL botUrl) throws ServiceException;

    String getLocalHeadHashByUrl(URL url) throws ServiceException;
}
