package com.lineate.xonix.mind.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.lineate.xonix.mind.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(exclude = {"botMatches", "tournament"})
@ToString(exclude = {"botMatches", "tournament"})
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "match")
public class MatchDb {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "match_seq_gen")
    @SequenceGenerator(name = "match_seq_gen", sequenceName = "match_seq", allocationSize = 1)
    private Integer id;

    @Enumerated
    Status status;

    @Builder.Default
    @OneToMany(mappedBy = "match", cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    List<BotMatchDb> botMatches = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tournament_id")
    TournamentDb tournament;

    @NonNull
    Long duration;

    @NonNull
    Double percent;

    @Builder.Default
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public MatchDb(Status status, List<BotDb> bots, Long duration, Double percent) {
        this.status = status;
        this.botMatches = new ArrayList<>();
        bots.forEach(this::addBot);
        this.duration = duration;
        this.percent = percent;
        this.createdAt = LocalDateTime.now();
    }

    public List<BotDb> getBots() {
        return botMatches.stream()
                .distinct()
                .map(BotMatchDb::getBot)
                .collect(Collectors.toList());
    }

    public void addBot(BotDb botDb) {
        BotMatchDb botMatchDb = new BotMatchDb(botDb, this);
        this.botMatches.add(botMatchDb);
        botDb.addBotMatch(botMatchDb);
    }

    public void addBotMatch(BotMatchDb botMatchDb) {
        this.botMatches.add(botMatchDb);
    }

}
