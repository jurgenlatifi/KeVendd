import { Ionicons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { router, useFocusEffect } from "expo-router";
import React, { useCallback, useRef, useState } from "react";
import {
  ActivityIndicator,
  Image,
  Keyboard,
  Pressable,
  SafeAreaView,
  StyleSheet,
  Text,
  TextInput,
  TouchableWithoutFeedback,
  View,
} from "react-native";
import MapView, { Marker } from "react-native-maps";
import { fetchParkingLots, ParkingLot } from "@/services/parkingService";

export default function MapGuestScreen() {
  const mapRef = useRef<MapView | null>(null);

  const [parkingLots, setParkingLots] = useState<ParkingLot[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState("");
  const [searchPin, setSearchPin] = useState<ParkingLot | null>(null);
  const [showSuggestions, setShowSuggestions] = useState(false);

  // ── Fetch parking lots from the backend on every tab focus ──
  useFocusEffect(
    useCallback(() => {
      let cancelled = false;

      (async () => {
        try {
          setLoading(true);
          const data = await fetchParkingLots();
          if (!cancelled) setParkingLots(data);
        } catch (err) {
          console.warn("Failed to load parking lots", err);
        } finally {
          if (!cancelled) setLoading(false);
        }
      })();

      return () => {
        cancelled = true;
      };
    }, [])
  );

  // ── Search helpers ──

  const startsWithSearch = (value: string, text: string) => {
    return value
      .toLowerCase()
      .split(" ")
      .some((word) => word.startsWith(text));
  };

  const filteredSuggestions = parkingLots.filter((lot) => {
    const text = searchText.trim().toLowerCase();
    if (!text) return false;

    return (
      startsWithSearch(lot.name, text) ||
      (lot.zone ? startsWithSearch(lot.zone, text) : false)
    );
  });

  const focusLocation = (lot: ParkingLot) => {
    setSearchPin(lot);
    Keyboard.dismiss();
    setShowSuggestions(false);

    mapRef.current?.animateToRegion(
      {
        latitude: lot.latitude,
        longitude: lot.longitude,
        latitudeDelta: 0.006,
        longitudeDelta: 0.006,
      },
      700
    );
  };

  const handleSearch = () => {
    const text = searchText.trim().toLowerCase();
    if (!text) return;

    const found = parkingLots.find(
      (lot) =>
        startsWithSearch(lot.name, text) ||
        (lot.zone ? startsWithSearch(lot.zone, text) : false)
    );

    if (found) {
      setSearchText(found.name);
      focusLocation(found);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <MapView
        ref={mapRef}
        provider="google"
        style={styles.map}
        initialRegion={{
          latitude: 41.3275,
          longitude: 19.8189,
          latitudeDelta: 0.018,
          longitudeDelta: 0.018,
        }}
        showsUserLocation={false}
        showsMyLocationButton={false}
        toolbarEnabled={false}
      >
        {parkingLots.map((lot) => (
          <Marker
            key={lot.id}
            coordinate={{
              latitude: lot.latitude,
              longitude: lot.longitude,
            }}
            title={lot.name}
            description={lot.zone ?? ""}
            onPress={() => {
              setSearchText("");
              focusLocation(lot);
            }}
          />
        ))}

        {searchPin && (
          <Marker
            coordinate={{
              latitude: searchPin.latitude,
              longitude: searchPin.longitude,
            }}
            pinColor="red"
            title={searchPin.name}
            description={searchPin.zone ?? ""}
          />
        )}
      </MapView>

      {loading && (
        <View style={styles.loadingOverlay}>
          <ActivityIndicator size="large" color="#ED0000" />
        </View>
      )}

      <TouchableWithoutFeedback
        onPress={() => {
          Keyboard.dismiss();
          setShowSuggestions(false);
        }}
        accessible={false}
      >
        <View style={StyleSheet.absoluteFill} pointerEvents="box-none">
          <View style={styles.logoWrapper}>
            <Image
              source={require("../../assets/black_logo.png")}
              style={styles.logo}
              resizeMode="contain"
            />
          </View>

          <View style={styles.searchBox}>
            <Ionicons name="search" size={20} color="#000000" />

            <TextInput
              placeholder="Kërko"
              placeholderTextColor="#979797"
              style={styles.searchInput}
              value={searchText}
              onChangeText={(text) => {
                setSearchText(text);
                setShowSuggestions(true);
              }}
              onFocus={() => setShowSuggestions(true)}
              returnKeyType="search"
              onSubmitEditing={handleSearch}
            />

            <Pressable onPress={handleSearch}>
              <Ionicons name="arrow-forward-circle" size={24} color="#ED0000" />
            </Pressable>
          </View>

          {showSuggestions && filteredSuggestions.length > 0 && (
            <View style={styles.suggestionsBox}>
              {filteredSuggestions.map((lot) => (
                <Pressable
                  key={lot.id}
                  style={styles.suggestionItem}
                  onPress={() => {
                    setSearchText(lot.name);
                    focusLocation(lot);
                  }}
                >
                  <Ionicons name="location" size={18} color="#ED0000" />

                  <View style={styles.suggestionTextWrapper}>
                    <Text style={styles.suggestionTitle}>{lot.name}</Text>
                    <Text style={styles.suggestionAddress}>{lot.zone ?? ""}</Text>
                  </View>
                </Pressable>
              ))}
            </View>
          )}

          <View style={styles.authPanel}>
            <Pressable onPress={() => router.replace("/(auth)/login")}>
              <LinearGradient
                colors={["#3080FF", "#00358B", "#00358B", "#3080FF"]}
                start={{ x: 0, y: 0 }}
                end={{ x: 1, y: 0 }}
                style={styles.authButton}
              >
                <Text style={styles.authButtonText}>Kyçu</Text>
              </LinearGradient>
            </Pressable>

            <Text style={styles.orText}>OSE</Text>

            <Pressable onPress={() => router.replace("/(auth)/register")}>
              <LinearGradient
                colors={["#3080FF", "#00358B", "#00358B", "#3080FF"]}
                start={{ x: 0, y: 0 }}
                end={{ x: 1, y: 0 }}
                style={styles.authButton}
              >
                <Text style={styles.authButtonText}>Regjistrohu</Text>
              </LinearGradient>
            </Pressable>
          </View>
        </View>
      </TouchableWithoutFeedback>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#ECECEC",
  },

  map: {
    ...StyleSheet.absoluteFillObject,
  },

  loadingOverlay: {
    position: "absolute",
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "rgba(255,255,255,0.3)",
    zIndex: 50,
  },

  logoWrapper: {
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

  searchBox: {
    position: "absolute",
    top: 136,
    left: 25,
    right: 25,
    height: 46,
    borderRadius: 18,
    backgroundColor: "#FFFFFF",
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 14,
    shadowColor: "#000",
    shadowOpacity: 0.2,
    shadowRadius: 7,
    elevation: 6,
    zIndex: 30,
  },

  searchInput: {
    flex: 1,
    marginLeft: 10,
    fontSize: 15,
    color: "#000",
    paddingVertical: 0,
  },

  suggestionsBox: {
    position: "absolute",
    top: 188,
    left: 25,
    right: 25,
    backgroundColor: "#FFFFFF",
    borderRadius: 16,
    paddingVertical: 6,
    shadowColor: "#000",
    shadowOpacity: 0.2,
    shadowRadius: 7,
    elevation: 7,
    zIndex: 25,
  },

  suggestionItem: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 14,
    paddingVertical: 10,
  },

  suggestionTextWrapper: {
    marginLeft: 10,
    flex: 1,
  },

  suggestionTitle: {
    fontSize: 14,
    fontWeight: "600",
    color: "#000000",
  },

  suggestionAddress: {
    fontSize: 12,
    color: "#777777",
    marginTop: 2,
  },

  authPanel: {
    position: "absolute",
    left: 0,
    right: 0,
    bottom: 0,
    height: 210,
    backgroundColor: "#000000",
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    paddingHorizontal: 27,
    paddingTop: 36,
    paddingBottom: 20,
    alignItems: "center",
    shadowColor: "#000",
    shadowOffset: { width: 2, height: -3 },
    shadowOpacity: 0.25,
    shadowRadius: 13,
    elevation: 12,
  },

  authButton: {
    width: 386,
    height: 47,
    borderRadius: 30,
    alignItems: "center",
    justifyContent: "center",
  },

  authButtonText: {
    color: "#FFFFFF",
    fontSize: 20.5,
    fontWeight: "600",
    textAlign: "center",
  },

  orText: {
    color: "#FFFFFF",
    fontSize: 15.5,
    fontWeight: "600",
    textAlign: "center",
    marginTop: 9,
    marginBottom: 10,
  },
});