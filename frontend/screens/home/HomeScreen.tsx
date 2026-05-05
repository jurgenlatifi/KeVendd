import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import React, { useEffect, useState } from "react";
import {
  Image,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import Svg, { Circle } from "react-native-svg";

import BottomTabBar from "../../components/navigation/BottomTabBar";

const TOTAL_SECONDS = 10 * 60;

export default function HomeScreen() {
  const [secondsLeft, setSecondsLeft] = useState(TOTAL_SECONDS);

  // change later (kur lidhim me backend)
  const hasNotifications = false;

  useEffect(() => {
    const timer = setInterval(() => {
      setSecondsLeft((prev) => {
        if (prev <= 0) return TOTAL_SECONDS;
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  const minutes = Math.floor(secondsLeft / 60);
  const seconds = secondsLeft % 60;
  const timeText = `${minutes}:${seconds.toString().padStart(2, "0")}`;

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        
        {/* HEADER */}
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
                router.push("/notifications");
                } else {
                router.push("/no-notifications");
                }
            }}
          >
            <Ionicons name="notifications-outline" size={28} color="#fff" />
            {hasNotifications && <View style={styles.notificationDot} />}
          </Pressable>
        </View>

        {/* COUNTDOWN */}
        <View style={styles.countdownWrapper}>
          <CountdownRing secondsLeft={secondsLeft} />
          <Text style={styles.countdownText}>{timeText}</Text>
        </View>

        {/* CURRENT RESERVATION */}
        <Text style={styles.sectionTitle}>Rezervimi Aktual</Text>

        <View style={styles.currentCard}>
          <View style={styles.imagePlaceholder} />

          <View style={styles.currentInfo}>
            <Text style={styles.currentName}>Avni Rustemi Parking</Text>
            <Text style={styles.currentAddress}>
              Sheshi Avni Rustemi, Tiranë
            </Text>

            <View style={styles.statusBadge}>
              <Text style={styles.statusText}>Hapur</Text>
            </View>
          </View>

          <View style={styles.spotsBadge}>
            <Text style={styles.spotsText}>5 vende</Text>
          </View>
        </View>

        {/* PARTNERS */}
        <Text style={styles.sectionTitle}>Bashkëpunimet Tona</Text>

        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
          <PartnerCard
            spaces="10 vende të lira"
            spacesColor="#6ACA6A"
            name="TopTani Shopping Center"
            address="Rruga Abdi Toptani, Tiranë"
            price="200ALL/ Ora"
          />

          <PartnerCard
            spaces="3 vende të lira"
            spacesColor="#ED0000"
            name="Parkimi Nentokesore"
            address="Rruga Ibrahim Rugova, Tiranë"
            price="200ALL/ Ora"
          />
        </ScrollView>
      </ScrollView>

      <BottomTabBar activeTab="home" />
    </SafeAreaView>
  );
}

/* COUNTDOWN RING */
function CountdownRing({ secondsLeft }: { secondsLeft: number }) {
  const size = 82;
  const strokeWidth = 12;
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

/* PARTNER CARD */
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

/* STYLES */
const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: "#000" },

  scrollContent: {
    paddingTop: 50,
    paddingHorizontal: 20,
    paddingBottom: 120,
  },

  header: {
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
    height: 60,
  },

  bellWrapper: {
    position: "absolute",
    right: 10,
    top: 20,
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

  countdownWrapper: {
    position: "absolute",
    right: 20,
    top: 130,
    width: 82,
    height: 82,
    alignItems: "center",
    justifyContent: "center",
  },

  countdownText: {
    position: "absolute",
    color: "#fff",
    fontSize: 14,
    fontWeight: "700",
  },

  sectionTitle: {
    color: "#fff",
    fontSize: 26,
    marginTop: 65,
    marginBottom: 15,
  },

  currentCard: {
    backgroundColor: "#323232",
    borderRadius: 15,
    flexDirection: "row",
    padding: 10,
    height: 130,
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
    fontSize: 16,
    fontWeight: "600",
  },

  currentAddress: {
    color: "#9D9D9D",
    fontSize: 12,
    marginTop: 5,
  },

  statusBadge: {
    marginTop: 55,
    backgroundColor: "rgba(106,202,106,0.3)",
    borderRadius: 50,
    paddingHorizontal: 10,
    paddingVertical: 3,
    alignSelf: "flex-start",
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
    paddingVertical: 3,
  },

  spotsText: {
    color: "#fff",
    fontSize: 10,
  },

  partnerCard: {
    width: 170,
    backgroundColor: "#323232",
    borderRadius: 15,
    padding: 8,
    marginRight: 15,
  },

  partnerImagePlaceholder: {
    height: 90,
    borderRadius: 8,
    backgroundColor: "#555",
  },

  partnerBadge: {
    marginTop: -15,
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
    fontSize: 14,
    fontWeight: "700",
    marginTop: 10,
  },

  partnerAddress: {
    color: "#9D9D9D",
    fontSize: 10,
  },

  partnerPrice: {
    color: "#fff",
    fontSize: 14,
    fontWeight: "700",
    marginTop: 10,
  },
});