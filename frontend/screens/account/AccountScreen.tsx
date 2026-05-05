import { Ionicons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { router } from "expo-router";
import React, { useState } from "react";
import {
  Image,
  Keyboard,
  Modal,
  Pressable,
  SafeAreaView,
  ScrollView,
  Text,
  TextInput,
  TouchableWithoutFeedback,
  View,
} from "react-native";

import ScreenHeader from "../../components/common/ScreenHeader";
import BottomTabBar from "../../components/navigation/BottomTabBar";
import styles from "../../constants/styles/accountStyle";

export default function AccountScreen() {
  const [plateModalVisible, setPlateModalVisible] = useState(false);
  const [paymentModalVisible, setPaymentModalVisible] = useState(false);
  const [logoutModalVisible, setLogoutModalVisible] = useState(false);
  const [deleteModalVisible, setDeleteModalVisible] = useState(false);
  const [photoOptionsVisible, setPhotoOptionsVisible] = useState(false);

  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [phone, setPhone] = useState("");

  const [plates, setPlates] = useState(["AB 123 JK"]);
  const [vehicleType, setVehicleType] = useState("");
  const [registeredOwner, setRegisteredOwner] = useState("");
  const [newPlate, setNewPlate] = useState("");

  const [payments, setPayments] = useState(["username@gmail.com"]);
  const [paymentEmail, setPaymentEmail] = useState("");
  const [paymentPassword, setPaymentPassword] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [isPrimaryPlate, setIsPrimaryPlate] = useState(false);
  const [isPrimaryPayment, setIsPrimaryPayment] = useState(false);

  const handleAddPlate = () => {
    if (!newPlate.trim()) return;

    setPlates([...plates, newPlate.trim().toUpperCase()]);
    setVehicleType("");
    setRegisteredOwner("");
    setNewPlate("");
    setIsPrimaryPlate(false);
    setPlateModalVisible(false);
  };

  const handleAddPayment = () => {
    if (!paymentEmail.trim()) return;

    setPayments([...payments, paymentEmail.trim()]);
    setPaymentEmail("");
    setPaymentPassword("");
    setIsPrimaryPayment(false);
    setShowPassword(false);
    setPaymentModalVisible(false);
  };

  return (
    <SafeAreaView style={styles.container}>
      <TouchableWithoutFeedback onPress={Keyboard.dismiss} accessible={false}>
        <View style={{ flex: 1 }}>
          <ScrollView
            contentContainerStyle={styles.scrollContent}
            showsVerticalScrollIndicator={false}
            bounces={false}
            keyboardShouldPersistTaps="handled"
          >
            <ScreenHeader />

            <View style={styles.content}>
              <View style={styles.avatarWrapper}>
                <Ionicons name="person-outline" size={58} color="#000000" />

                <Pressable
                  style={styles.editButton}
                  onPress={() => setPhotoOptionsVisible(true)}
                >
                  <Ionicons name="pencil" size={17} color="#FFFFFF" />
                </Pressable>
              </View>

              <View style={styles.section}>
                <Text style={styles.sectionTitle}>Të Dhënat</Text>

                <View style={styles.row}>
                  <View style={styles.inputGroup}>
                    <Text style={styles.label}>Emri</Text>

                    <View style={styles.inputPressable} pointerEvents="box-none">
                      <TextInput
                        style={styles.inputText}
                        placeholder="Emri"
                        placeholderTextColor="#FFFFFF"
                        value={name}
                        onChangeText={setName}
                      />
                    </View>
                  </View>

                  <View style={styles.inputGroup}>
                    <Text style={styles.label}>Mbiemri</Text>

                    <View style={styles.inputPressable} pointerEvents="box-none">
                      <TextInput
                        style={styles.inputText}
                        placeholder="Mbiemri"
                        placeholderTextColor="#FFFFFF"
                        value={surname}
                        onChangeText={setSurname}
                      />
                    </View>
                  </View>
                </View>

                <View style={styles.row}>
                  <View style={styles.inputGroup}>
                    <Text style={styles.labelLarge}>Nr. i Telefonit</Text>

                    <View style={styles.inputPressable} pointerEvents="box-none">
                      <TextInput
                        style={styles.inputText}
                        placeholder="Nr."
                        placeholderTextColor="#FFFFFF"
                        value={phone}
                        onChangeText={setPhone}
                        keyboardType="phone-pad"
                      />
                    </View>
                  </View>

                  <Pressable
                    style={styles.passwordPressable}
                    onPress={() => router.push("/change-password" as any)}
                  >
                    <LinearGradient
                      colors={["#3080FF", "#00358B", "#00358B", "#3080FF"]}
                      locations={[0, 0.378, 0.6298, 1]}
                      start={{ x: 0, y: 0 }}
                      end={{ x: 1, y: 0 }}
                      style={styles.passwordButton}
                    >
                      <Text style={styles.passwordButtonText}>
                        Ndrysho Fjalëkalimin
                      </Text>
                    </LinearGradient>
                  </Pressable>
                </View>

                <Text style={styles.sectionTitle}>Targat e mia</Text>

                <View style={styles.cardGroup}>
                  {plates.map((plate, index) => (
                    <View key={index} style={styles.darkCard}>
                      <Text style={styles.cardText}>{plate}</Text>
                    </View>
                  ))}

                  <Pressable
                    style={styles.lightCard}
                    onPress={() => setPlateModalVisible(true)}
                  >
                    <Ionicons
                      name="add-circle-outline"
                      size={13}
                      color="#000000"
                    />
                    <Text style={styles.lightCardText}>Shto Targë</Text>
                  </Pressable>
                </View>

                <Text style={styles.sectionTitle}>Pagesa</Text>

                <View style={styles.cardGroup}>
                  {payments.map((payment, index) => (
                    <View key={index} style={styles.darkCard}>
                      <Image
                        source={require("../../assets/paypal.png")}
                        style={{
                          width: 28,
                          height: 28,
                          resizeMode: "contain",
                        }}
                      />

                      <Text style={styles.paymentText}>
                        PayPal Account: {payment}
                      </Text>

                      <Ionicons name="bookmark" size={18} color="#FFFFFF" />
                    </View>
                  ))}

                  <Pressable
                    style={styles.lightCard}
                    onPress={() => setPaymentModalVisible(true)}
                  >
                    <Ionicons
                      name="add-circle-outline"
                      size={13}
                      color="#000000"
                    />
                    <Text style={styles.lightCardText}>Shto mënyrë pagese</Text>
                  </Pressable>
                </View>

                <Pressable
                  style={styles.invoiceRow}
                  onPress={() => router.push("/invoices" as any)}
                >
                  <Text style={styles.invoiceText}>Faturat e mia</Text>
                  <Ionicons name="chevron-forward" size={22} color="#FFFFFF" />
                </Pressable>

                <View style={styles.actionButtonsRow}>
                  <Pressable
                    style={styles.logoutButton}
                    onPress={() => setLogoutModalVisible(true)}
                  >
                    <Ionicons
                      name="log-out-outline"
                      size={22}
                      color="#E50000"
                    />
                    <Text style={styles.logoutText}>Dil</Text>
                  </Pressable>

                  <Pressable
                    style={styles.deleteButton}
                    onPress={() => setDeleteModalVisible(true)}
                  >
                    <Text style={styles.deleteText}>Fshi Llogarinë</Text>
                  </Pressable>
                </View>
              </View>
            </View>
          </ScrollView>
        </View>
      </TouchableWithoutFeedback>

      <BottomTabBar activeTab="account" />

      <Modal visible={photoOptionsVisible} transparent animationType="fade">
        <Pressable
          style={styles.overlay}
          onPress={() => setPhotoOptionsVisible(false)}
        >
          <Pressable style={styles.photoOptionsModal}>
            <Pressable
              style={styles.photoOptionRow}
              onPress={() => {
                setPhotoOptionsVisible(false);
                console.log("View photo");
              }}
            >
              <Ionicons name="eye-outline" size={22} color="#000000" />
              <Text style={styles.photoOptionText}>Shiko foton</Text>
            </Pressable>

            <View style={styles.photoOptionDivider} />

            <Pressable
              style={styles.photoOptionRow}
              onPress={() => {
                setPhotoOptionsVisible(false);
                console.log("Change photo");
              }}
            >
              <Ionicons name="camera-outline" size={22} color="#000000" />
              <Text style={styles.photoOptionText}>Ndrysho foton</Text>
            </Pressable>
          </Pressable>
        </Pressable>
      </Modal>

      <Modal visible={plateModalVisible} transparent animationType="fade">
        <TouchableWithoutFeedback onPress={Keyboard.dismiss} accessible={false}>
          <View style={styles.overlay}>
            <View style={styles.plateModal}>
              <Pressable
                style={styles.closeButton}
                onPress={() => setPlateModalVisible(false)}
              >
                <Ionicons name="close" size={18} color="#000000" />
              </Pressable>

              <Text style={styles.modalTitle}>Shto targë të re</Text>

              <Text style={styles.modalLabel}>Lloji i Mjetit :</Text>
              <TextInput
                style={styles.modalInput}
                value={vehicleType}
                onChangeText={setVehicleType}
              />

              <Text style={styles.modalLabel}>Pronari i Regjistruar :</Text>
              <TextInput
                style={styles.modalInput}
                value={registeredOwner}
                onChangeText={setRegisteredOwner}
              />

              <Text style={styles.modalLabel}>Targa</Text>
              <TextInput
                style={styles.modalInput}
                value={newPlate}
                onChangeText={setNewPlate}
                autoCapitalize="characters"
              />

              <Pressable
                style={styles.checkboxRow}
                onPress={() => setIsPrimaryPlate(!isPrimaryPlate)}
              >
                <View style={styles.modalCheckbox}>
                  {isPrimaryPlate && (
                    <Ionicons name="checkmark" size={10} color="#000000" />
                  )}
                </View>
                <Text style={styles.checkboxText}>
                  Zgjidhe si targën primare
                </Text>
              </Pressable>

              <Pressable onPress={handleAddPlate}>
                <LinearGradient
                  colors={["#3080FF", "#00358B", "#00358B", "#3080FF"]}
                  locations={[0, 0.378, 0.6298, 1]}
                  start={{ x: 0, y: 0 }}
                  end={{ x: 1, y: 0 }}
                  style={styles.modalSaveButton}
                >
                  <Text style={styles.modalSaveText}>Ruaj</Text>
                </LinearGradient>
              </Pressable>
            </View>
          </View>
        </TouchableWithoutFeedback>
      </Modal>

      <Modal visible={paymentModalVisible} transparent animationType="fade">
        <TouchableWithoutFeedback onPress={Keyboard.dismiss} accessible={false}>
          <View style={styles.overlay}>
            <View style={styles.paymentModal}>
              <Pressable
                style={styles.closeButton}
                onPress={() => setPaymentModalVisible(false)}
              >
                <Ionicons name="close" size={18} color="#000000" />
              </Pressable>

              <Text style={styles.modalTitle}>Shto mënyrë pagese</Text>

              <Text style={styles.modalLabel}>
                E-maili i llogarisë në PayPal
              </Text>
              <TextInput
                style={styles.modalInput}
                keyboardType="email-address"
                autoCapitalize="none"
                value={paymentEmail}
                onChangeText={setPaymentEmail}
              />

              <Text style={styles.modalLabel}>Fjalëkalimi</Text>
              <View style={styles.modalPasswordInput}>
                <TextInput
                  style={styles.passwordModalTextInput}
                  secureTextEntry={!showPassword}
                  value={paymentPassword}
                  onChangeText={setPaymentPassword}
                />

                <Pressable onPress={() => setShowPassword(!showPassword)}>
                  <Ionicons
                    name={showPassword ? "eye-outline" : "eye-off-outline"}
                    size={22}
                    color="#000000"
                  />
                </Pressable>
              </View>

              <Pressable
                style={styles.checkboxRow}
                onPress={() => setIsPrimaryPayment(!isPrimaryPayment)}
              >
                <View style={styles.modalCheckbox}>
                  {isPrimaryPayment && (
                    <Ionicons name="checkmark" size={10} color="#000000" />
                  )}
                </View>

                <Text style={styles.checkboxText}>
                  Zgjidhe si pagesën primare
                </Text>
              </Pressable>

              <Pressable onPress={handleAddPayment}>
                <LinearGradient
                  colors={["#3080FF", "#00358B", "#00358B", "#3080FF"]}
                  locations={[0, 0.378, 0.6298, 1]}
                  start={{ x: 0, y: 0 }}
                  end={{ x: 1, y: 0 }}
                  style={styles.modalSaveButton}
                >
                  <Text style={styles.modalSaveText}>Ruaj</Text>
                </LinearGradient>
              </Pressable>
            </View>
          </View>
        </TouchableWithoutFeedback>
      </Modal>

      <Modal visible={logoutModalVisible} transparent animationType="fade">
        <View style={styles.overlay}>
          <View style={styles.logoutModal}>
            <Text style={styles.logoutTitle}>Doni të dilni?</Text>

            <View style={styles.logoutButtonsRow}>
              <Pressable
                style={styles.cancelButton}
                onPress={() => setLogoutModalVisible(false)}
              >
                <Text style={styles.cancelText}>Cancel</Text>
              </Pressable>

              <Pressable
                style={styles.confirmLogoutButton}
                onPress={() => {
                  setLogoutModalVisible(false);
                  router.replace("/login" as any);
                }}
              >
                <Text style={styles.confirmLogoutText}>Dil</Text>
              </Pressable>
            </View>
          </View>
        </View>
      </Modal>

      <Modal visible={deleteModalVisible} transparent animationType="fade">
        <View style={styles.overlay}>
          <View style={styles.logoutModal}>
            <Text style={styles.logoutTitle}>Doni të fshini llogarinë?</Text>

            <View style={styles.logoutButtonsRow}>
              <Pressable
                style={styles.cancelButton}
                onPress={() => setDeleteModalVisible(false)}
              >
                <Text style={styles.cancelText}>Cancel</Text>
              </Pressable>

              <Pressable
                style={styles.confirmLogoutButton}
                onPress={() => {
                  setDeleteModalVisible(false);
                  router.replace("/login" as any);
                }}
              >
                <Text style={styles.confirmLogoutText}>Fshi</Text>
              </Pressable>
            </View>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}