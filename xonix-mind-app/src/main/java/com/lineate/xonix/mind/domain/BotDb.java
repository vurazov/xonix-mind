package com.lineate.xonix.mind.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(exclude = {"botMatches", "tournaments"})
@ToString(exclude = {"botMatches", "tournaments"})
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bot")
public class BotDb {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bot_seq_gen")
    @SequenceGenerator(name = "bot_seq_gen", sequenceName = "bot_seq", allocationSize = 1)
    private Integer id;


    @Builder.Default
    @OneToMany(mappedBy = "bot", cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BotMatchDb> botMatches = new ArrayList<>();

    @Builder.Default
    @ManyToMany(mappedBy = "bots", fetch = FetchType.EAGER)
    private List<TournamentDb> tournaments = new ArrayList<>();

    @NonNull
    private String name;

    @NonNull
    private String srcUrl;

    public BotDb(List<MatchDb> match, String name, String srcUrl) {
        this.botMatches = new ArrayList<>();
        match.forEach(this::addMatch);
        this.name = name;
        this.srcUrl = srcUrl;
    }

    public List<MatchDb> getMatches() {
        return this.botMatches.stream()
                .map(BotMatchDb::getMatch)
                .collect(Collectors.toList());
    }

    private void addMatch(MatchDb matchDb) {
        BotMatchDb botMatchDb = new BotMatchDb(this, matchDb);
        this.botMatches.add(botMatchDb);
        matchDb.addBotMatch(botMatchDb);
    }

    public void addBotMatch(BotMatchDb botMatchDb) {
        this.botMatches.add(botMatchDb);
    }


}
