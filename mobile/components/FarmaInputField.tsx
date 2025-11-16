import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  StyleSheet,
  TextInputProps,
} from "react-native";

interface InputFieldProps extends TextInputProps {
  label: string;
  required?: boolean;
  value?: string;
  onChangeText?: (text: string) => void;
}

export default function InputField({
  label,
  required,
  value = "",
  onChangeText,
  style,
  ...props
}: InputFieldProps) {
  const [isFocused, setIsFocused] = useState(false);

  // Mostrar o placeholder customizado quando não houver texto
  const showOverlay = value.length === 0;

  return (
    <View style={styles.container}>
      <View style={styles.inputWrapper}>
        {/* Overlay que parece placeholder */}
        {showOverlay && (
          <View pointerEvents="none" style={styles.overlay}>
            <Text style={styles.overlayText}>
              {label}
              {required && <Text style={styles.asterisk}>*</Text>}
            </Text>
          </View>
        )}

        <TextInput
          style={[styles.input, style]}
          value={value}
          onChangeText={onChangeText}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          {...props}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: "100%",
    marginVertical: 8,
  },
  inputWrapper: {
    width: 284,
    position: "relative",
    backgroundColor: "#fff",
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#e6e6e6",
    overflow: "hidden",
  },
  input: {
    height: 52,
    paddingLeft: 16, // espaço para o overlay ficar colado na esquerda
    paddingRight: 16,
    fontSize: 16,
    color: "#000",
  },
  overlay: {
    position: "absolute",
    left: 12,
    top: 0,
    bottom: 0,
    justifyContent: "center",
  },
  overlayText: {
    fontSize: 16,
    color: "#777",
  },
  asterisk: {
    color: "red",
    marginLeft: 2,
  },
});
