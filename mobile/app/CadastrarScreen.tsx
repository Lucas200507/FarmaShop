// ...existing code...
import FarmaInputField from '@/components/FarmaInputField';
import FarmaButton from '@/components/FarmaButtonBlue';
import FarmaButtonGreen from '@/components/FarmaButtonGreen';
import FarmaHeader from '@/components/FarmaHeader';
import React, { useState } from 'react';
import { View, Text, StyleSheet, Platform, StatusBar as RNStatusBar, TouchableOpacity } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useRouter } from "expo-router";

export default function CadastrarScreen() {
    const insets = useSafeAreaInsets();
    const topInset = insets.top || (Platform.OS === 'android' ? (RNStatusBar.currentHeight ?? 0) : 0);

    const [email, setEmail] = useState("");
    const router = useRouter();

    return (
        <View style={[styles.container, { paddingTop: topInset }]}>
            <FarmaHeader title="Para uma melhor experiência, entre ou cadastre-se" />

      <View style={styles.content}>

        <Text style={styles.text}>
          Já tenho uma conta
        </Text>

        <FarmaButton title="Entrar" onPress={() => {router.navigate("/LoginScreen")}} />
        
        <Text style={styles.text}>
          Não tenho uma conta
        </Text>

        <FarmaButtonGreen title="Cadastrar" onPress={() => {}} />
        
        <TouchableOpacity onPress={() => {router.navigate("/Cadastros/CadastrarEmpresaScreen")}}>
            <Text style={styles.link}>
                Cadastrar empresa
            </Text>
        </TouchableOpacity>


        <Text style={styles.link}>
          Continuar sem uma conta
        </Text>
        
        <TouchableOpacity onPress={() => {router.navigate("/Cadastros/CadastrarEntregadorScreen")}}>
            <Text style={styles.link}>
                Cadastrar para entregador
            </Text>
        </TouchableOpacity>
        
      </View>
    
    </View>
  );
}

const styles = StyleSheet.create({
  container: { 
    flex: 1, 
    backgroundColor: '#000038', 
    alignItems: 'center'
  },
  header: {
    width: '100%',
    backgroundColor: '#fff',
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 60,
    marginBottom: 40,
    // bordas inferiores arredondadas
    borderBottomLeftRadius: 60,
    borderBottomRightRadius: 60,
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 6,
  },
 logo: {
  width: 96,
  height: 96,
  position: 'absolute',
  top: 95, // ajusta este valor conforme a altura do seu header
  alignSelf: 'center',
  backgroundColor: '#000038',
  borderRadius: 48,
  padding: 10,
  zIndex: 10, // garante que fique por cima
},
  title: { 
    fontSize: 24, 
    fontWeight: '500', 
    color: '#000038' },
  content: { 
    flex: 1,
    alignItems: 'center',
    gap: 20,
    padding: 20, 
    marginTop: 8 
  },
  text: {
    color: '#fff',
    fontWeight: '400',
    fontSize: 30,
  }, 
  link: {
    color: '#6AD7BF',
    fontWeight: '400',
    fontSize: 25,
  }
});
