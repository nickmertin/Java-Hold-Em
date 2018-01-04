/*
 * Name: Nicholas Mertin
 * Course: ICS3U
 * Game.java
 * Provides the main controller for the game
 */

package javaholdem;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Game {
    public Player[] players;
    public float ante;
    public float[] blinds;
    public Hand current;
    public int dealer;
    public Stack<Card> deck;
    public Proxy proxy;

    // Creates a new game from the given set of players, ante, set of blinds, and proxy
    public Game(Player[] players, float ante, float[] blinds, Proxy proxy){
        this.players = players;
        this.ante = ante;
        this.blinds = blinds;
        this.proxy = proxy;
        dealer = 0;
        regenerateDeck();
    }

    // Fills the deck with the 52 standard cards in a random order
    public void regenerateDeck() {
        deck = new Stack<>();
        LinkedList<Card> l = new LinkedList<>();
        for (Rank r : Rank.values())
            for (Suit s : Suit.values())
                l.add(new Card(s, r));
        Random rand = new Random();
        while (l.size() != 0) {
            int i = rand.nextInt(l.size());
            deck.push(l.get(i));
            l.remove(i);
        }
    }

    // Continues the game, providing the player context for the next turn to be played
    @SuppressWarnings("unchecked")
    public PlayerContext next() {
        // If this is not the first call
        if (current != null) {
            // Pay out balance for each sidepot that has only 1 player left in it, then filter out these sidepots
            for (Sidepot p : current.sidepots)
                if (p.players.length == 1)
                    p.players[0].player.balance += p.balance;
            List<Sidepot> newSidepots = Arrays.stream(current.sidepots).filter(p -> p.players.length > 1).collect(Collectors.toList());
            // Merge sidepots with identical player lists
            for (int i = 0; i < newSidepots.size() - 1; i++) {
                Sidepot _i = newSidepots.get(i);
                for (int j = i + 1; j < newSidepots.size(); j++) {
                    Sidepot _j = newSidepots.get(j);
                    if (Arrays.equals(_i.players, _j.players)) {
                        _i.balance += _j.balance;
                        _i.bet += _j.bet;
                        newSidepots.remove(j);
                    }
                }
            }
            Sidepot[] ans = new Sidepot[newSidepots.size()];
            for (int i = 0; i < newSidepots.size(); i++)
                ans[i] = newSidepots.get(i);
            current.sidepots = ans;
            // Pay out balance to winner if only one player left
            if (current.players.length == 1) {
                current.players[0].player.balance += current.mainPotBalance;
                current = null;
            } else {
                // If full round with no more betting, move on to next stage
                if (current.lastToBet == current.players[current.player].player && !current.firstTurn) {
                    switch (current.stage) {
                        // First round of betting
                        case 0:
                            deck.pop();
                            current.communityCards = new Card[]{deck.pop(), deck.pop(), deck.pop()};
                            current.lastToBet = current.players[current.player = (dealer + 1) % current.players.length].player;
                            break;
                        // Second or third round of betting
                        case 1:
                        case 2:
                            deck.pop();
                            current.communityCards = Arrays.copyOf(current.communityCards, current.communityCards.length + 1);
                            current.communityCards[current.communityCards.length - 1] = deck.pop();
                            break;
                        // Last round of betting
                        case 3:
                            // Clear the screen and print out info
                            proxy.clear();
                            proxy.log("Community cards:");
                            Arrays.stream(current.communityCards).map(Card::toString).forEach(proxy::log);
                            for (PlayerInHand player : current.players)
                                proxy.log(String.format("%s: %s, %s", player.player.toString(), player.card1.toString(), player.card2.toString()));
                            proxy.log("Main pot");
                            showdown(current.players, current.mainPotBalance);
                            for (Sidepot sidepot : current.sidepots) {
                                proxy.log(sidepot.toString());
                                showdown(sidepot.players, sidepot.balance);
                            }
                            proxy.pause();
                            // Reset to start new hand
                            current = null;
                            break;
                    }
                    if (current != null)
                        ++current.stage;
                }
                if (current != null)
                    current.firstTurn = false;
            }
        }
        if (current == null) {
            ArrayList<Player> pNew = new ArrayList<>();
            for (int i = 0; i < players.length; i++) {
                Player p = players[i];
                int bPos = (i + players.length - dealer - 1) % players.length;
                if (p.balance >= ante + (bPos >= 0 && bPos < blinds.length ? blinds[bPos] : 0))
                    pNew.add(p);
            }
            players = new Player[pNew.size()];
            for (int i = 0; i < pNew.size(); i++)
                players[i] = pNew.get(i);
            if (players.length == 1)
                return null;
            regenerateDeck();
            PlayerInHand[] monikers = new PlayerInHand[players.length];
            float pot = 0;
            for (int i = 0; i < players.length; i++) {
                int bPos = (i + players.length - dealer - 1) % players.length;
                float bet = ante + (bPos >= 0 && bPos < blinds.length ? blinds[bPos] : 0);
                players[i].balance -= bet;
                pot += bet;
                monikers[i] = new PlayerInHand(players[i], deck.pop(), deck.pop());
            }
            current = new Hand(monikers, pot, ante + (blinds.length > 0 ?blinds[blinds.length - 1] : 0), (blinds.length + 1) % players.length);
            ++dealer;
        }
        PlayerContext ctx = new PlayerContext(this, current.players[current.player]);
        ++current.player;
        current.player %= current.players.length;
        return ctx;
    }

    // Runs a showdown for the given set of players, awarding the given amount of money
    @SuppressWarnings("unchecked")
    private void showdown(PlayerInHand[] players, float pot) {
        Object[] handFilters = new Object[] {
                // Straight flush
                (Predicate<Card[]>) cards -> {
                    for (int i = 0; i < cards.length - 4; i++) {
                        boolean g = true;
                        for (int j = i + 1; j < i + 5; j++)
                            if (cards[j].suit != cards[i].suit || cards[j].rank.ordinal() - cards[i].rank.ordinal() != j - i) {
                                g = false;
                                break;
                            }
                        if (g)
                            return true;
                    }
                    return false;
                },
                // Four of a kind
                (Predicate<Card[]>) cards -> {
                    for (int i = 0; i < cards.length - 3; i++) {
                        boolean g = true;
                        for (int j = i + 1; j < i + 4; j++)
                            if (cards[j].rank != cards[i].rank) {
                                g = false;
                                break;
                            }
                        if (g)
                            return true;
                    }
                    return false;
                },
                // Full house
                (Predicate<Card[]>) cards -> {
                    for (int i = 0; i < cards.length - 1; i++)
                        if (cards[i].rank == cards[i + 1].rank)
                            for (int j = 0; j < cards.length - 2; j++)
                                if ((j < i - 2 || j > i + 1) && cards[j].rank == cards[j + 1].rank && cards[j].rank == cards[j + 2].rank)
                                    return true;
                    return false;
                },
                // Flush
                (Predicate<Card[]>) cards -> {
                    for (int i = 0; i < cards.length - 4; i++) {
                        boolean g = true;
                        for (int j = i + 1; j < i + 5; j++)
                            if (cards[j].suit != cards[i].suit) {
                                g = false;
                                break;
                            }
                        if (g)
                            return true;
                    }
                    return false;
                },
                // Straight
                (Predicate<Card[]>) cards -> {
                    for (int i = 0; i < cards.length - 4; i++) {
                        boolean g = true;
                        for (int j = i + 1; j < i + 5; j++)
                            if (cards[j].rank.ordinal() - cards[i].rank.ordinal() != j - i) {
                                g = false;
                                break;
                            }
                        if (g)
                            return true;
                    }
                    return false;
                },
                // Three of a kind
                (Predicate<Card[]>) cards -> {
                    for (int i = 0; i < cards.length - 2; i++)
                        if (cards[i].rank == cards[i + 1].rank && cards[i].rank == cards[i + 2].rank)
                            return true;
                    return false;
                },
                // Two pair
                (Predicate<Card[]>) cards -> {
                    int c = 0;
                    for (int i = 0; i < cards.length - 1; i++)
                        if (cards[i].rank == cards[i + 1].rank) {
                            ++c;
                            ++i;
                        }
                    return c > 1;
                },
                // One pair
                (Predicate<Card[]>) cards -> {
                    for (int i = 0; i < cards.length - 1; i++)
                        if (cards[i].rank == cards[i + 1].rank)
                            return true;
                    return false;
                }
        };
        String[] names = new String[] {
                "straight flush",
                "four of a kind",
                "full house",
                "flush",
                "straight",
                "three of a kind",
                "two pair",
                "one pair"
        };
        List<PlayerInHand> matching = Arrays.asList(players);
        for (int i = 0; i < handFilters.length; i++) {
            matching = processHand(matching, (Predicate<Card[]>)handFilters[i], names[i]);
            if (matching == null)
                break;
        }
        // If there are still multiple players, resort to highest card
        if (matching != null) {
            int max = -1;
            for (PlayerInHand player : matching) {
                int v = Integer.max(player.card1.rank.ordinal(), player.card2.rank.ordinal());
                if (v > max)
                    max = v;
            }
            final int max1 = max;
            List<PlayerInHand> winners = matching.stream().filter(p -> Integer.max(p.card1.rank.ordinal(), p.card2.rank.ordinal()) == max1).collect(Collectors.toList());
            // If there is a tie, go to second highest card
            if (winners.size() != 1) {
                max = -1;
                for (PlayerInHand player : matching) {
                    int v = Integer.min(player.card1.rank.ordinal(), player.card2.rank.ordinal());
                    if (v > max)
                        max = v;
                }
                final int max2 = max;
                winners = winners.stream().filter(p -> Integer.min(p.card1.rank.ordinal(), p.card2.rank.ordinal()) == max2).collect(Collectors.toList());
            }
            // Pay out to winners, splitting if there is a tie
            for (PlayerInHand p : winners)
                p.player.balance += pot / winners.size();
        }

    }

    // Checks the given set of players against the given hand pattern predicate
    private List<PlayerInHand> processHand(List<PlayerInHand> players, Predicate<Card[]> predicate, String handName) {
        List<PlayerInHand> matching = players.stream().filter(new HandMatch(predicate, handName)).collect(Collectors.toList());
        if (matching.size() == 1) {
            matching.get(0).player.balance += current.mainPotBalance;
            return null;
        }
        else if (matching.size() > 1) {
            current.players = new PlayerInHand[matching.size()];
            for (int i = 0; i < matching.size(); i++)
                current.players[i] = matching.get(i);
            return matching;
        }
        return players;
    }

    // Represents a hand pattern predicate
    final class HandMatch implements Predicate<PlayerInHand> {
        private Predicate<Card[]> p;
        private String n;

        // Creates a new predicate from the given core predicate and hand name
        public HandMatch(Predicate<Card[]> predicate, String handName) {
            p = predicate;
            n = handName;
        }

        // Wraps the test function of the core predicate
        @Override
        public boolean test(PlayerInHand player) {
            Card[] hand = Arrays.copyOf(current.communityCards, 7);
            hand[5] = player.card1;
            hand[6] = player.card2;
            Arrays.sort(hand, (x, y) -> x.rank.compareTo(y.rank));
            boolean result = p.test(hand);
            if (result)
                proxy.log(String.format("%s has a %s!", player.player.name, n));
            return result;
        }
    }

    // Represents a floating-point number predicate with variable low, high, and increment values
    public static final class DynamicFloatRange implements Predicate<Float> {
        private float l, h, i;

        // Creates a new predicate from the given values
        public DynamicFloatRange(float lowBound, float highBound, float increment) {
            l = lowBound;
            h = highBound;
            i = increment;
        }

        // Tests the given floating-point number
        @Override
        public boolean test(Float f) {
            return f > l && f < h && f % i == 0;
        }
    }

    // Represents a player moniker predicate that requires a set minimum balance
    static final class DynamicPlayerMinBalance implements Predicate<PlayerInHand> {
        private float m;

        // Creates a predicate from the given minimum balance
        public DynamicPlayerMinBalance(float min) {
            m = min;
        }

        // Tests the given player moniker
        @Override
        public boolean test(PlayerInHand p) {
            return p.player.balance >= m;
        }
    }
}
