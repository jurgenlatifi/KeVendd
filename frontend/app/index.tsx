import { Redirect } from "expo-router";

// replace it me the real thing nga supabase kush po ben lidhjen
const isLoggedIn = false;

export default function Index() {
  if (isLoggedIn) {
    return <Redirect href="/map" />;
  }

  return <Redirect href="/map-guest" />;
}