import { Ionicons } from "@expo/vector-icons";
import { useFocusEffect } from "expo-router";
import React, { useCallback, useState } from "react";
import {
  ActivityIndicator,
  Image,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

import BackButton from "@/components/common/BackButton";
import api from "@/api";

// ── Types ─────────────────────────────────────────────────────────────────────

type NotificationType =
  | "CHECK_IN_CONFIRMATION"
  | "EXPIRY_WARNING"
  | "EXPIRY_REACHED"
  | "UNPAID_REMINDER"
  | "OTP"
  | "RESERVATION_CONFIRMATION";

type DeliveryStatus = "PENDING" | "DELIVERED" | "FAILED";
type NotificationChannel = "PUSH" | "SMS";

type Notification = {
  id: number;
  message: string;
  type: NotificationType;
  channel: NotificationChannel;
  deliveryStatus: DeliveryStatus;
  sentAt: string | number[] | null;
  createdAt: string | number[];
};

// ── Helpers ───────────────────────────────────────────────────────────────────

function parseLocalDateTime(raw: string | number[] | null): Date | null {
  if (!raw) return null;
  if (Array.isArray(raw)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = raw;
    return new Date(year, month - 1, day, hour, minute, second);
  }
  return new Date(raw);
}

function formatDate(raw: string | number[]): string {
  const date = parseLocalDateTime(raw);
  if (!date) return "";
  return date.toLocaleDateString("sq-AL", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

function formatTime(raw: string | number[] | null): string {
  const date = parseLocalDateTime(raw);
  if (!date) return "";
  return date.toLocaleTimeString("sq-AL", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

const TYPE_LABELS: Record<NotificationType, string> = {
  CHECK_IN_CONFIRMATION:    "Konfirmim Check-In",
  EXPIRY_WARNING:           "Paralajmërim Skadimi",
  EXPIRY_REACHED:           "Rezervimi Skadoi",
  UNPAID_REMINDER:          "Kujtesë Pagese",
  OTP:                      "Kod OTP",
  RESERVATION_CONFIRMATION: "Konfirmim Rezervimi",
};

const STATUS_COLOR: Record<DeliveryStatus, string> = {
  PENDING:   "#F0A500",
  DELIVERED: "#6ACA6A",
  FAILED:    "#ED0000",
};

// ── Screen ────────────────────────────────────────────────────────────────────

export default function NotificationsScreen() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading]             = useState(true);
  const [error, setError]                 = useState<string | null>(null);

  useFocusEffect(
    useCallback(() => {
      let cancelled = false;

      const fetch = async () => {
        setLoading(true);
        setError(null);
        try {
          const { data } = await api.get<Notification[]>("/notifications/me");
          if (!cancelled) setNotifications(data);
        } catch {
          if (!cancelled) setError("Ndodhi një gabim. Provoni përsëri.");
        } finally {
          if (!cancelled) setLoading(false);
        }
      };

      fetch();
      return () => { cancelled = true; };
    }, [])
  );

  // Group by formatted date string
  const grouped = notifications.reduce<Record<string, Notification[]>>(
    (acc, item) => {
      const label = formatDate(item.createdAt);
      if (!acc[label]) acc[label] = [];
      acc[label].push(item);
      return acc;
    },
    {}
  );

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <BackButton />

        <Image
          source={require("../../assets/logo.png")}
          style={styles.logo}
          resizeMode="contain"
        />
      </View>

      {/* ── Body ── */}
      {loading ? (
        <View style={styles.centeredState}>
          <ActivityIndicator size="large" color="#6ACA6A" />
        </View>

      ) : error ? (
        <View style={styles.centeredState}>
          <Ionicons name="cloud-offline-outline" size={60} color="#555" />
          <Text style={styles.stateText}>{error}</Text>
        </View>

      ) : notifications.length === 0 ? (
        <View style={styles.emptyContainer}>
          <Image
            source={require("../../assets/notifications.png")}
            style={styles.emptyImage}
            resizeMode="contain"
          />
          <Text style={styles.emptyText}>
            Ju nuk keni asnjë njoftim për momentin.
          </Text>
        </View>

      ) : (
        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.scrollContent}
        >
          <Text style={styles.title}>Njoftime</Text>

          {Object.entries(grouped).map(([date, items]) => (
            <View key={date} style={styles.group}>
              <Text style={styles.dateText}>{date}</Text>

              <View style={styles.notificationsBox}>
                {items.map((item, index) => (
                  <View key={item.id}>
                    <View style={styles.notificationRow}>
                      {/* Icon */}
                      <View style={styles.iconCircle}>
                        <Ionicons
                          name="notifications-outline"
                          size={24}
                          color="#fff"
                        />
                      </View>

                      {/* Text */}
                      <View style={styles.textContainer}>
                        <View style={styles.rowHeader}>
                          <Text style={styles.name}>
                            {TYPE_LABELS[item.type] ?? item.type}
                          </Text>
                          <Text style={styles.time}>
                            {formatTime(item.sentAt ?? item.createdAt)}
                          </Text>
                        </View>

                        <Text style={styles.message}>{item.message}</Text>

                        {/* Delivery status dot */}
                        <View style={styles.statusRow}>
                          <View
                            style={[
                              styles.statusDot,
                              { backgroundColor: STATUS_COLOR[item.deliveryStatus] },
                            ]}
                          />
                          <Text style={styles.statusText}>
                            {item.deliveryStatus === "DELIVERED"
                              ? "Dërguar"
                              : item.deliveryStatus === "PENDING"
                              ? "Në pritje"
                              : "Dështuar"}
                          </Text>
                        </View>
                      </View>
                    </View>

                    {index !== items.length - 1 && <View style={styles.line} />}
                  </View>
                ))}
              </View>
            </View>
          ))}
        </ScrollView>
      )}
    </SafeAreaView>
  );
}

// ── Styles ────────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#000",
  },

  header: {
    position: "absolute",
    top: 55,
    alignSelf: "center",
    alignItems: "center",
    justifyContent: "center",
  },

  logo: {
    width: 145,
    height: 55,
  },

  scrollContent: {
    paddingTop: 140,
    paddingBottom: 120,
  },

  centeredState: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    gap: 16,
  },

  stateText: {
    color: "#9D9D9D",
    fontSize: 14,
    textAlign: "center",
    paddingHorizontal: 32,
  },

  emptyContainer: {
    position: "absolute",
    top: "39%",
    alignSelf: "center",
    alignItems: "center",
    width: "100%",
  },

  emptyImage: {
    width: 100,
    height: 100,
    marginBottom: 35,
  },

  emptyText: {
    maxWidth: "90%",
    fontSize: 14,
    lineHeight: 27,
    letterSpacing: -1,
    color: "#fff",
    textAlign: "center",
  },

  title: {
    color: "#fff",
    fontSize: 30,
    fontWeight: "400",
    marginLeft: 25,
    marginBottom: 18,
  },

  group: {
    marginBottom: 14,
  },

  dateText: {
    color: "#fff",
    fontSize: 18,
    fontWeight: "500",
    marginLeft: 25,
    marginBottom: 8,
  },

  notificationsBox: {
    backgroundColor: "#323232",
    paddingVertical: 10,
  },

  notificationRow: {
    flexDirection: "row",
    paddingHorizontal: 15,
    paddingVertical: 12,
  },

  iconCircle: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: "#2A77F1",
    alignItems: "center",
    justifyContent: "center",
    marginRight: 20,
  },

  textContainer: {
    flex: 1,
  },

  rowHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },

  name: {
    color: "#fff",
    fontSize: 18,
    fontWeight: "400",
  },

  time: {
    color: "#fff",
    fontSize: 11,
    fontWeight: "400",
  },

  message: {
    color: "rgba(209, 209, 209, 0.78)",
    fontSize: 14,
    lineHeight: 16,
    marginTop: 4,
    paddingRight: 8,
  },

  statusRow: {
    flexDirection: "row",
    alignItems: "center",
    marginTop: 6,
    gap: 5,
  },

  statusDot: {
    width: 7,
    height: 7,
    borderRadius: 4,
  },

  statusText: {
    color: "#9D9D9D",
    fontSize: 11,
  },

  line: {
    height: 1,
    backgroundColor: "rgba(255,255,255,0.27)",
    marginLeft: 85,
    marginRight: 15,
  },
});