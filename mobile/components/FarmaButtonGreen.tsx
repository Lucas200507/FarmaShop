import React from 'react';
import { TouchableOpacity, Text, StyleSheet } from 'react-native';

export default function FarmaButtonGreen({ title, onPress }: { title: string; onPress: () => void }) {
  return (
    <TouchableOpacity onPress={onPress}>
      <Text style={styles.botao}>{title}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  botao: {
    backgroundColor: '#6AD7BF',
    borderRadius: 30,
    width: 225,
    height: 51,
    textAlign: 'center',
    color: '#000000',
    fontWeight: '500',
    fontSize: 30,
  },
  
});