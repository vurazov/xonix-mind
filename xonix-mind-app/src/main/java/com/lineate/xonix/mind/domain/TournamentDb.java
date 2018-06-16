package com.lineate.xonix.mind.domain;

import com.lineate.xonix.mind.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"bots", "matches"})
@ToString(exclude = {"bots", "matches"})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tournament")
public class TournamentDb {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tournament_seq_gen")
    @SequenceGenerator(name = "tournament_seq_gen", sequenceName = "tournament_seq", allocationSize = 1)
    private Integer id;

    @Enumerated
    private Status status;

    @Builder.Default
    @ManyToMany(cascade = {
            CascadeType.PERSIST,
    }, fetch = FetchType.EAGER)
    @JoinTable(name = "tournament_bot",
            joinColumns = @JoinColumn(name = "tournament_id"),
            inverseJoinColumns = @JoinColumn(name = "bot_id")
    )
    private List<BotDb> bots = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tournament", cascade = {CascadeType.PERSIST}, fetch = FetchType.EAGER)
    List<MatchDb> matches = new ArrayList<>();

    @NonNull
    private Long duration;

    @NonNull
    private Double percent;
}
