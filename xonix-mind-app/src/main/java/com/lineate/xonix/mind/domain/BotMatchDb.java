package com.lineate.xonix.mind.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bot_match")
public class BotMatchDb {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "bot_id")
    private Integer botId;

    @Column(name = "match_id")
    private Integer matchId;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("botId")
    private BotDb bot;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("matchId")
    private MatchDb match;

    @Builder.Default
    @Column
    private Integer score = 0;

    @Builder.Default
    @Column
    private Boolean isBuildSuccess = true;

    @Column
    private String version;

    public BotMatchDb(BotDb bot, MatchDb match) {
        this.bot = bot;
        this.match = match;
        this.botId = bot.getId();
        this.matchId = match.getId();
    }

}
