import { createFileRoute } from "@tanstack/react-router";
import { Navbar } from "@/components/site/Navbar";
import { Hero } from "@/components/site/Hero";
import { Programs } from "@/components/site/Programs";
import { Academy } from "@/components/site/Academy";
import { Schedule } from "@/components/site/Schedule";
import { Pricing } from "@/components/site/Pricing";
import { Contact } from "@/components/site/Contact";

export const Route = createFileRoute("/")({
  component: Index,
});

function Index() {
  return (
    <main className="bg-background text-foreground">
      <Navbar />
      <Hero />
      <Academy />
      <Programs />
      <Schedule />
      <Pricing />
      <Contact />
    </main>
  );
}
