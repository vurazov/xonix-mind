package com.lineate.xonix.mind.mapper;

import com.lineate.xonix.mind.domain.BotDb;
import com.lineate.xonix.mind.domain.MatchDb;
import com.lineate.xonix.mind.domain.TournamentDb;
import com.lineate.xonix.mind.domain.dto.BotDto;
import com.lineate.xonix.mind.domain.dto.GetAllBotsDto;
import com.lineate.xonix.mind.domain.dto.MatchDto;
import com.lineate.xonix.mind.domain.dto.StateBotDto;
import com.lineate.xonix.mind.domain.dto.StateMatchDto;
import com.lineate.xonix.mind.domain.dto.StateTournamentDto;
import com.lineate.xonix.mind.domain.dto.TournamentDto;
import com.lineate.xonix.mind.service.GameStateService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeToken;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TypeMapper {

    private ModelMapper mapper;

    private GameStateService gameStateService;

    public TypeMapper(){
        mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        mapper.addMappings(new PropertyMap<MatchDb, MatchDto>() {
            @Override
            protected void configure() {
            }
        });
        mapper.addMappings(new PropertyMap<MatchDto, MatchDb>() {
            @Override
            protected void configure() {
            }
        });
        mapper.addMappings(new PropertyMap<BotDto, BotDb>() {
            @Override
            protected void configure() {
            }
        });
        mapper.addMappings(new PropertyMap<BotDb, StateBotDto>() {
            @Override
            protected void configure() {
            }
        });
        mapper.addMappings(new PropertyMap<StateBotDto, BotDb>() {
            @Override
            protected void configure() {
            }
        });
        mapper.addMappings(new PropertyMap<BotDb, BotDto>() {
            @Override
            protected void configure() {
            }
        });
        mapper.addMappings(new PropertyMap<BotDto, GetAllBotsDto>() {
            @Override
            protected void configure() {
            }
        });
        mapper.addMappings(new PropertyMap<TournamentDto, TournamentDb>() {
            @Override
            protected void configure() {
            }
        });
        mapper.addMappings(new PropertyMap<TournamentDb, StateTournamentDto>() {
            @Override
            protected void configure() {
            }
        });
        mapper.getTypeMap(MatchDto.class, MatchDb.class)
                .setPostConverter(context -> {
                    MatchDb target = context.getDestination();
                    log.info("post-converter fixing OrderItem parent links {}", target);
                    if (target.getBots() != null) {
                        target.getBots().stream().forEach((item) -> item.getMatches().add(target));
                    }
                    return target;
                });
    }

    public MatchDb mapToMatch(MatchDto matchDto) {
        return this.mapper.map(matchDto, MatchDb.class);
    }

    public TournamentDb mapToTournamentDb(TournamentDto tournamentDto) {
        return this.mapper.map(tournamentDto, TournamentDb.class);
    }

    public StateTournamentDto mapToStateTournamentDto(TournamentDb tournamentDb) {
        return this.mapper.map(tournamentDb, StateTournamentDto.class);
    }

    public MatchDb mapToMatch(StateMatchDto matchDto) {
        return this.mapper.map(matchDto, MatchDb.class);
    }

    public StateMatchDto mapToStateMatchDto(MatchDb match) {
        return this.mapper.map(match, StateMatchDto.class);
    }

    public BotDb mapToBot(BotDto botDto) {
        return this.mapper.map(botDto, BotDb.class);
    }

    public GetAllBotsDto mapToGetAllBotsDto(BotDb botDb) {
        return this.mapper.map(botDb, GetAllBotsDto.class);
    }

    public BotDto mapToBotDto(BotDb botDb) {
        return this.mapper.map(botDb, BotDto.class);
    }

    public List<StateMatchDto>  toMatchDtoList(List<MatchDb> matches) {
        return this.mapper.map(matches, new TypeToken<List<StateMatchDto>>() {
        }.getType());
    }

    public List<BotDto>  toBotDtoList(List<BotDb> bots) {
        return this.mapper.map(bots, new TypeToken<List<BotDto>>() {
        }.getType());
    }

    public List<BotDb>  toBotDdbList(List<StateBotDto> bots) {
        return this.mapper.map(bots, new TypeToken<List<BotDb>>() {
        }.getType());
    }

    public List<GetAllBotsDto>  toGetAllBotsDtoList(List<BotDb> bots) {
        return this.mapper.map(bots, new TypeToken<List<GetAllBotsDto>>() {
        }.getType());
    }
}
