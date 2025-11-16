import React from "react";
import { Text, Image, StyleSheet, View, ScrollView } from "react-native";
import FarmaPromoCard from "./components/FarmaPromoCard";
import FarmaCard from "./components/FarmaCard";
import FarmaTagButton from "./components/FarmaTagButton";

export default function HomeScreen() {
    const farmaPromoSection = () => {
        return (
            <>
                <Text style={styles.title}> Promoções </Text>
            <ScrollView 
                horizontal={true} 
                showsHorizontalScrollIndicator={false}
                style={styles.promoScroll}
                contentContainerStyle={{gap: 12}}
            >
            <FarmaPromoCard 
                name="Medicamentos"
                price="19,90"
                action={()=>{}}
            />
            <FarmaPromoCard 
                name="Medicamentos"
                price="19,90"
                action={()=>{}}
            />
            <FarmaPromoCard 
                name="Medicamentos"
                price="19,90"
                action={()=>{}}
            />
            </ScrollView>
            </>
        )
    }

    const createFarmaCarrossel = (title: string) => {
        return (
            <>
            <View
  style={{
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',

  }}
>
  <Text
    style={[styles.title, { fontSize: 16, flexShrink: 1 }]}
    numberOfLines={1}
    ellipsizeMode="tail"
  >
    {title}
  </Text>
  <FarmaTagButton label="Ver mais" action={() => {}} />
</View>

        <ScrollView 
                horizontal={true} 
                showsHorizontalScrollIndicator={false}
                style={styles.promoScroll}
                contentContainerStyle={{gap: 12}}
            >
            <FarmaCard
                label="Título"
                price="18,00"
                action={()=>{}}
            />
            <FarmaCard
                label="Título"
                price="18,00"
                action={()=>{}}
            />
            <FarmaCard
                label="Título"
                price="18,00"
                action={()=>{}}
            />
        </ScrollView>
        </>
        );
    }
  return (
    <>
        <View style={styles.header}>
        <Image
                style={styles.nameImage}
                source={require("@/assets/images/FarmaShopName.png")}
              />
        <Image
                style={styles.userIcon}
                source={require("@/assets/images/FarmaShopUserIcon.png")}
              />
        </View>
        <ScrollView style={styles.content}>
            {farmaPromoSection()}
            {createFarmaCarrossel("Medicamentos a partir de 15,99")}
            {createFarmaCarrossel("Produtos em destaque")}

        </ScrollView>
    </>
  );
}

const styles = StyleSheet.create({
    header: {
        paddingTop: 50,
        paddingHorizontal: 12,
        display: 'flex',
        flexDirection: 'row',
        justifyContent: 'space-between',
        backgroundColor: '#000038',
    },
    userIcon: {
        width: 47,
        height: 47,
    },
    nameImage: {
        width: 113,
        height: 39
    },
    content: {
        display: 'flex',
        flexDirection: 'column',
        paddingHorizontal: 12,
        marginTop: 20,
    },
    title: {
        fontSize: 32,
        fontWeight: '500',
    },
    promoScroll: {
        marginTop: 20,
        marginBottom: 40,
        
    }
});