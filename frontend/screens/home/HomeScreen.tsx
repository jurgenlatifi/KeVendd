import { Ionicons } from "@expo/vector-icons";
import { router, useFocusEffect } from "expo-router";
import React, { useCallback, useEffect, useRef, useState } from "react";
import {
  ActivityIndicator,
  Animated,
  Dimensions,
  Image,
  Modal,
  PanResponder,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import Svg, { Circle } from "react-native-svg";

import {
  fetchMyHistory,
  ReservationData,
} from "@/services/reservationService";
import { fetchParkingLots, ParkingLot } from "@/services/parkingService";

const TOTAL_SECONDS = 10 * 60;

export default function HomeScreen() {
  // Data from backend
  const [activeReservation, setActiveReservation] = useState<ReservationData | null>(null);
  const [secondsLeft, setSecondsLeft] = useState<number | null>(null);
  const [partnerParkings, setPartnerParkings] = useState<ParkingLot[]>([]);
  const [loading, setLoading] = useState(true);
  const [expiredModalVisible, setExpiredModalVisible] = useState(false);

  const hasNotifications = false;

  // ── Fetch data from backend on every focus ──
  useFocusEffect(
    useCallback(() => {
      let cancelled = false;

      (async () => {
        try {
          setLoading(true);

          const [history, parkings] = await Promise.all([
            fetchMyHistory().catch(() => [] as ReservationData[]),
            fetchParkingLots({ availableOnly: true }).catch(() => [] as ParkingLot[]),
          ]);

          if (cancelled) return;

          const active = history.find(
            (r) => r.status === "CONFIRMED" || r.status === "SOFT_HOLD"
          );
          setActiveReservation(active ?? null);
          setPartnerParkings(parkings.slice(0, 4));
        } catch (err) {
          console.warn("Failed to load home data", err);
        } finally {
          if (!cancelled) setLoading(false);
        }
      })();

      return () => {
        cancelled = true;
      };
    }, [])
  );

  // ── Compute remaining time from holdExpiresAt ──
  useEffect(() => {
    let timer: ReturnType<typeof setInterval> | null = null;

    if (activeReservation?.holdExpiresAt) {
      const expirationDate = new Date(activeReservation.holdExpiresAt).getTime();

      const updateTimer = () => {
        const remaining = Math.max(0, Math.floor((expirationDate - Date.now()) / 1000));
        setSecondsLeft(remaining);

        if (remaining <= 0) {
          setActiveReservation(null);
          setSecondsLeft(null);
          setExpiredModalVisible(true);
          if (timer) clearInterval(timer);
        }
      };

      updateTimer();
      timer = setInterval(updateTimer, 1000);
    } else {
      setSecondsLeft(null);
    }

    return () => {
      if (timer) clearInterval(timer);
    };
  }, [activeReservation]);

  const minutes = secondsLeft !== null ? Math.floor(secondsLeft / 60) : 0;
  const seconds = secondsLeft !== null ? secondsLeft % 60 : 0;
  const timeText = `${minutes}:${seconds.toString().padStart(2, "0")}`;

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <View style={styles.logoContainer}>
            <Image
              source={require("../../assets/logo.png")}
              style={styles.logo}
              resizeMode="contain"
            />
          </View>

          <Pressable
            style={styles.bellWrapper}
            onPress={() => {
              if (hasNotifications) {
                router.push("(screens)/notifications");
              } else {
                router.push("(screens)/no-notifications");
              }
            }}
          >
            <Ionicons name="notifications-outline" size={28} color="#fff" />
            {hasNotifications && <View style={styles.notificationDot} />}
          </Pressable>
        </View>

        <Text style={styles.sectionTitle}>Rezervimi Aktual</Text>

        {loading ? (
          <ActivityIndicator
            size="large"
            color="#ED0000"
            style={{ marginVertical: 20 }}
          />
        ) : activeReservation ? (
          <View style={styles.currentCard}>
            <View style={styles.imagePlaceholder} />

            <View style={styles.currentInfo}>
              <Text style={styles.currentName}>
                {activeReservation.parkingName}
              </Text>
              <Text style={styles.currentAddress}>
                {activeReservation.totalCost
                  ? `${activeReservation.totalCost} ALL`
                  : ""}
              </Text>

              <View style={styles.statusBadge}>
                <Text style={styles.statusText}>
                  {activeReservation.status === "CONFIRMED"
                    ? "Konfirmuar"
                    : "Në pritje"}
                </Text>
              </View>
            </View>

            <View style={styles.spotsBadge}>
              <Text style={styles.spotsText}>
                {activeReservation.spotsReserved} vende
              </Text>
            </View>
          </View>
        ) : (
          <View style={styles.currentCard}>
            <View style={styles.imagePlaceholder} />
            <View style={styles.currentInfo}>
              <Text style={styles.currentName}>Nuk keni rezervim aktual</Text>
              <Text style={styles.currentAddress}>
                Shkoni te harta për të gjetur një parkim
              </Text>
            </View>
          </View>
        )}

        <Text style={styles.sectionTitle}>Bashkëpunimet Tona</Text>

        {loading ? (
          <ActivityIndicator
            size="small"
            color="#ED0000"
            style={{ marginVertical: 20 }}
          />
        ) : (
          <ScrollView horizontal showsHorizontalScrollIndicator={false}>
            {partnerParkings.map((lot) => (
              <Pressable
                key={lot.id}
                onPress={() =>
                  router.push({
                    pathname: "/parking-detail",
                    params: { parkingId: String(lot.id) },
                  })
                }
              >
                <PartnerCard
                  spaces={`${lot.availableSpots} vende të lira`}
                  spacesColor={lot.availableSpots > 5 ? "#6ACA6A" : "#ED0000"}
                  name={lot.name}
                  address={lot.zone ?? ""}
                  price={`${lot.pricePerHour}ALL/ Ora`}
                />
              </Pressable>
            ))}

            {partnerParkings.length === 0 && (
              <Text style={{ color: "#9D9D9D", marginLeft: 10 }}>
                Nuk u gjetën parkime.
              </Text>
            )}
          </ScrollView>
        )}
      </ScrollView>

      {/* Draggable countdown rendered outside ScrollView so it floats freely */}
      {activeReservation && secondsLeft !== null && (
        <DraggableCountdown secondsLeft={secondsLeft} timeText={timeText} />
      )}

      <Modal visible={expiredModalVisible} transparent animationType="fade">
        <View style={styles.modalOverlay}>
          <View style={styles.expiredModal}>
            <View style={styles.expiredCircle}>
              <Ionicons name="time-outline" size={52} color="#FFFFFF" />
            </View>

            <Text style={styles.expiredTitle}>
              Koha e rezervimit përfundoi.
            </Text>

            <Text style={styles.expiredText}>
              Nëse nuk keni arritur në parkimin e zgjedhur, ju nuk do të keni
              një vend të rezervuar.
            </Text>

            <Pressable
              style={styles.okButton}
              onPress={() => setExpiredModalVisible(false)}
            >
              <Text style={styles.okButtonText}>Në rregull</Text>
            </Pressable>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

// ─── Draggable Countdown ───────────────────────────────────────────────────────

function DraggableCountdown({
  secondsLeft,
  timeText,
}: {
  secondsLeft: number;
  timeText: string;
}) {
  const WIDGET_SIZE = 82;
  const SCREEN_WIDTH = Dimensions.get("window").width;
  const SCREEN_HEIGHT = Dimensions.get("window").height;
  const SNAP_MARGIN = 12;

  const MIN_Y = 90;
  const MAX_Y = SCREEN_HEIGHT - WIDGET_SIZE - 78 - 10;

  const initialX = SCREEN_WIDTH - WIDGET_SIZE - SNAP_MARGIN;
  const initialY = 130;

  const pan = useRef(new Animated.ValueXY({ x: initialX, y: initialY })).current;
  const panRef = useRef({ x: initialX, y: initialY });

  useEffect(() => {
    const id = pan.addListener((val) => {
      panRef.current = val;
    });
    return () => pan.removeListener(id);
  }, [pan]);

  const clampY = (y: number) => Math.min(Math.max(y, MIN_Y), MAX_Y);

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onPanResponderGrant: () => {
        pan.setOffset({ x: panRef.current.x, y: panRef.current.y });
        pan.setValue({ x: 0, y: 0 });
      },
      onPanResponderMove: (_, gesture) => {
        pan.x.setValue(panRef.current.x + gesture.dx - panRef.current.x);
        const offsetY = (pan.y as any)._offset ?? 0;
        pan.y.setValue(
          Math.min(Math.max(gesture.dy, MIN_Y - offsetY), MAX_Y - offsetY)
        );
      },
      onPanResponderRelease: () => {
        pan.flattenOffset();

        const currentX = panRef.current.x;
        const currentY = clampY(panRef.current.y);

        const snapX =
          currentX + WIDGET_SIZE / 2 < SCREEN_WIDTH / 2
            ? SNAP_MARGIN
            : SCREEN_WIDTH - WIDGET_SIZE - SNAP_MARGIN;

        Animated.spring(pan, {
          toValue: { x: snapX, y: currentY },
          useNativeDriver: false,
          friction: 6,
          tension: 80,
        }).start();
      },
    })
  ).current;

  return (
    <Animated.View
      style={[
        styles.draggableContainer,
        { transform: pan.getTranslateTransform() },
      ]}
      {...panResponder.panHandlers}
    >
      <CountdownRing secondsLeft={secondsLeft} />
      <Text style={styles.countdownText}>{timeText}</Text>
    </Animated.View>
  );
}

// ─── Countdown Ring ────────────────────────────────────────────────────────────

function CountdownRing({ secondsLeft }: { secondsLeft: number }) {
  const size = 90;
  const strokeWidth = 18;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const progress = secondsLeft / TOTAL_SECONDS;
  const strokeDashoffset = circumference * (1 - progress);

  return (
    <Svg width={size} height={size}>
      <Circle
        cx={size / 2}
        cy={size / 2}
        r={radius}
        stroke="rgba(106,202,106,0.2)"
        strokeWidth={strokeWidth}
        fill="#000"
      />
      <Circle
        cx={size / 2}
        cy={size / 2}
        r={radius}
        stroke="rgba(106,202,106,0.9)"
        strokeWidth={strokeWidth}
        fill="transparent"
        strokeDasharray={circumference}
        strokeDashoffset={strokeDashoffset}
        strokeLinecap="round"
        rotation="-90"
        originX={size / 2}
        originY={size / 2}
      />
    </Svg>
  );
}

// ─── Partner Card ──────────────────────────────────────────────────────────────

function PartnerCard({ spaces, spacesColor, name, address, price }: any) {
  return (
    <View style={styles.partnerCard}>
      <View style={styles.partnerImagePlaceholder} />

      <View style={[styles.partnerBadge, { backgroundColor: spacesColor }]}>
        <Text style={styles.partnerBadgeText}>{spaces}</Text>
      </View>

      <Text style={styles.partnerName}>{name}</Text>
      <Text style={styles.partnerAddress}>{address}</Text>
      <Text style={styles.partnerPrice}>{price}</Text>
    </View>
  );
}

// ─── Styles ───────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#000" },

  scrollContent: {
    paddingTop: 50,
    paddingHorizontal: 20,
    paddingBottom: 120,
  },

  header: {
    top: -50,
    height: 70,
    alignItems: "center",
    justifyContent: "center",
  },

  logoContainer: {
    alignItems: "center",
    justifyContent: "center",
  },

  logo: {
    width: 145,
    height: 55,
  },

  bellWrapper: {
    position: "absolute",
    top: 10,
    right: 30,
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: "rgba(76,76,76,0.45)",
    alignItems: "center",
    justifyContent: "center",
  },

  notificationDot: {
    position: "absolute",
    right: 2,
    top: 2,
    width: 7,
    height: 7,
    borderRadius: 4,
    backgroundColor: "#ED0000",
  },

  // Draggable floating widget
  draggableContainer: {
    position: "absolute",
    width: 82,
    height: 82,
    alignItems: "center",
    justifyContent: "center",
    zIndex: 999,
  },

  countdownText: {
    position: "absolute",
    color: "#fff",
    fontSize: 14,
    fontWeight: "700",
  },

  sectionTitle: {
    color: "#fff",
    fontSize: 30,
    marginBottom: 15,
  },

  currentCard: {
    backgroundColor: "#323232",
    borderRadius: 15,
    flexDirection: "row",
    padding: 10,
    height: 130,
    marginBottom: 36,
  },

  imagePlaceholder: {
    width: 120,
    height: 110,
    borderRadius: 10,
    backgroundColor: "#555",
  },

  currentInfo: {
    marginLeft: 10,
    flex: 1,
  },

  currentName: {
    color: "#fff",
    fontSize: 20,
    fontWeight: "600",
    marginTop: 15,
  },

  currentAddress: {
    color: "#9D9D9D",
    fontSize: 12,
    marginTop: 5,
  },

  statusBadge: {
    backgroundColor: "rgba(106,202,106,0.3)",
    position: "absolute",
    bottom: 0,
    borderRadius: 20,
    paddingHorizontal: 10,
    paddingVertical: 4,
    height: 21,
  },

  statusText: {
    color: "#54E754",
    fontSize: 11,
  },

  spotsBadge: {
    position: "absolute",
    right: 10,
    bottom: 10,
    backgroundColor: "rgba(237,0,0,0.7)",
    borderRadius: 20,
    paddingHorizontal: 10,
    paddingVertical: 5,
    height: 21,
  },

  spotsText: {
    color: "#fff",
    fontSize: 10,
  },

  partnerCard: {
    width: 250,
    height: 260,
    backgroundColor: "#323232",
    borderRadius: 15,
    padding: 8,
    marginRight: 15,
  },

  partnerImagePlaceholder: {
    height: 135,
    borderRadius: 8,
    backgroundColor: "#555",
  },

  partnerBadge: {
    marginTop: -20,
    marginLeft: 5,
    alignSelf: "flex-start",
    borderRadius: 20,
    paddingHorizontal: 8,
    paddingVertical: 2,
  },

  partnerBadgeText: {
    color: "#fff",
    fontSize: 8,
  },

  partnerName: {
    color: "#fff",
    fontSize: 18,
    fontWeight: "600",
    marginTop: 20,
    marginLeft: 5,
  },

  partnerAddress: {
    color: "#9D9D9D",
    fontSize: 12,
    marginTop: 3,
    marginLeft: 5,
  },

  partnerPrice: {
    color: "#fff",
    fontSize: 18,
    fontWeight: "700",
    marginTop: 35,
    marginLeft: 5,
  },

  modalOverlay: {
    flex: 1,
    backgroundColor: "#ED0000",
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 24,
  },

  expiredModal: {
    width: "100%",
    backgroundColor: "#FFFFFF",
    borderRadius: 20,
    padding: 26,
    alignItems: "center",
  },

  expiredCircle: {
    width: 92,
    height: 92,
    borderRadius: 46,
    backgroundColor: "#ED0000",
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 22,
  },

  expiredTitle: {
    color: "#000000",
    fontSize: 18,
    fontWeight: "700",
    textAlign: "center",
    marginBottom: 12,
  },

  expiredText: {
    color: "#000000",
    fontSize: 14,
    textAlign: "center",
    lineHeight: 21,
  },

  okButton: {
    backgroundColor: "#ED0000",
    borderRadius: 24,
    paddingHorizontal: 28,
    paddingVertical: 12,
    marginTop: 24,
  },

  okButtonText: {
    color: "#FFFFFF",
    fontSize: 15,
    fontWeight: "700",
  },
});