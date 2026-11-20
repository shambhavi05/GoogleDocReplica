export interface DemoUser {
  name: string;
  email: string;
  description: string;
}

export const DEMO_USERS: DemoUser[] = [
  { name: 'Shambhavi', email: 'shambhavi@ajaia.com', description: 'Owns "Product Roadmap"' },
  { name: 'Bob', email: 'bob@agij.com', description: "Editor on Shambhavi's roadmap" },
  { name: 'Charlie', email: 'charlie@agij.com', description: "Viewer on Shambhavi's roadmap" },
];
