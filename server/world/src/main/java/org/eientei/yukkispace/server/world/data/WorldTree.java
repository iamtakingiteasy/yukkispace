package org.eientei.yukkispace.server.world.data;

import java.util.*;

/**
 * User: iamtakingiteasy
 * Date: 2013-08-21
 * Time: 10:36
 */
public class WorldTree implements Iterable<WorldTree.Node> {
    private Node root;

    public WorldTree(World world) {
        this.root = new Node(null, world);
    }

    public Node resolveNode(String ... names) {
        Node n = root;
        for (String s : names) {
            n = n.getChild(s);
            if (n == null) {
                break;
            }
        }
        return n;
    }

    public Node resolveNode(List<String> names) {
        Node n = root;
        for (String s : names) {
            n = n.getChild(s);
            if (n == null) {
                break;
            }
        }
        return n;
    }

    public Set<Node> flatten() {
        return root.flatten(-1);
    }

    public Set<Client> clients() {
        return root.flattenClients(-1);
    }

    public Set<Entity> entities() {
        return root.flattenEntities(-1);
    }

    public Set<Light> lights() {
        return root.flattenLights(-1);
    }

    public Set<Camera> cameras() {
        return root.flattenCameras(-1);
    }

    public Node getRoot() {
        return root;
    }

    public void addNode(Node node) {
        root.addChild(node);
    }

    @Override
    public Iterator<Node> iterator() {
        return flatten().iterator();
    }

    public static class Node extends Object3D implements Iterable<Node> {
        private World world;
        private List<String> wayUp = new ArrayList<String>();
        private Map<String, Node> nodes = new HashMap<String, Node>();
        private Map<String, Client> clients = new HashMap<String, Client>();
        private Map<String, Entity> entities = new HashMap<String, Entity>();
        private Map<String, Light> lights = new HashMap<String, Light>();
        private Map<String, Camera> cameras = new HashMap<String, Camera>();

        public Node(String nodeName, World world) {
            super(nodeName);
            this.world = world;
        }

        public Set<Node> flatten(int maxDepth) {
            Set<Node> ns = new HashSet<Node>(nodes.values());
            if (maxDepth > 0 || maxDepth == -1) {
                for (Node n : ns) {
                    ns.addAll(n.flatten(maxDepth-1));
                }
            }
            return ns;
        }

        public Set<Client> flattenClients(int maxDepth) {
            Set<Node> ns = new HashSet<Node>(nodes.values());
            Set<Client> result = new HashSet<Client>(clients.values());
            if (maxDepth > 0 || maxDepth == -1) {
                for (Node n : ns) {
                    result.addAll(n.flattenClients(maxDepth-1));
                }
            }
            return result;
        }

        public Set<Entity> flattenEntities(int maxDepth) {
            Set<Node> ns = new HashSet<Node>(nodes.values());
            Set<Entity> result = new HashSet<Entity>(entities.values());
            if (maxDepth > 0 || maxDepth == -1) {
                for (Node n : ns) {
                    result.addAll(n.flattenEntities(maxDepth-1));
                }
            }
            return result;
        }

        public Set<Light> flattenLights(int maxDepth) {
            Set<Node> ns = new HashSet<Node>(nodes.values());
            Set<Light> result = new HashSet<Light>(lights.values());
            if (maxDepth > 0 || maxDepth == -1) {
                for (Node n : ns) {
                    result.addAll(n.flattenLights(maxDepth-1));
                }
            }
            return result;
        }

        public Set<Camera> flattenCameras(int maxDepth) {
            Set<Node> ns = new HashSet<Node>(nodes.values());
            Set<Camera> result = new HashSet<Camera>(cameras.values());
            if (maxDepth > 0 || maxDepth == -1) {
                for (Node n : ns) {
                    result.addAll(n.flattenCameras(maxDepth-1));
                }
            }
            return result;
        }

        public List<String> getWayUp() {
            return Collections.unmodifiableList(wayUp);
        }

        public void setWayUp(List<String> list) {
            this.wayUp = list;
        }

        public Node getChild(String name) {
            return nodes.get(name);
        }

        public Client getClient(String name) {
            return clients.get(name);
        }

        public Entity getEntity(String name) {
            return entities.get(name);
        }

        public Light getLight(String name) {
            return lights.get(name);
        }

        public Camera getCamera(String name) {
            return cameras.get(name);
        }


        public void addChild(Node node) {
            if (node.getName() == null) {
                return;
            }
            nodes.put(node.getName(), node);
            List<String> way = new ArrayList<String>(wayUp);
            way.add(node.getName());
            node.setWayUp(way);
            node.setParentNode(this);
            node.setParentWorld(world);
        }

        public void addClient(Client client) {
            if (client.getName() == null) {
                return;
            }
            clients.put(client.getName(), client);
            client.setParentNode(this);
            client.setParentWorld(world);
        }

        public void addEntity(Entity entity) {
            if (entity.getName() == null) {
                return;
            }

            entities.put(entity.getName(), entity);
            entity.setParentNode(this);
            entity.setParentWorld(world);
        }

        public void addLight(Light light) {
            if (light.getName() == null) {
                return;
            }
            lights.put(light.getName(), light);
            light.setParentNode(this);
            light.setParentWorld(world);
        }

        public void addCamera(Camera camera) {
            if (camera.getName() == null) {
                return;
            }

            cameras.put(camera.getName(), camera);
            camera.setParentNode(this);
            camera.setParentWorld(world);
        }

        public void nullifyRecursive() {
            for (Node n : nodes.values()) {
                n.nullifyRecursive();
            }

            setWayUp(new ArrayList<String>());
            setParentNode(null);
            setParentWorld(null);

            for (Client c : clients.values()) {
                c.setParentNode(null);
                c.setParentWorld(null);
            }

            for (Entity e : entities.values()) {
                e.setParentNode(null);
                e.setParentWorld(null);
            }

            for (Light l : lights.values()) {
                l.setParentNode(null);
                l.setParentWorld(null);
            }

            for (Camera c : cameras.values()) {
                c.setParentNode(null);
                c.setParentWorld(null);
            }

            nodes.clear();
            clients.clear();
            entities.clear();
            lights.clear();
            cameras.clear();
        }

        @Override
        public Iterator<Node> iterator() {
            return flatten(-1).iterator();
        }

        public Node removeNode(String name) {
            Node old = nodes.remove(name);
            old.nullifyRecursive();
            return old;
        }

        public Client removeClient(String name) {
            Client old = clients.remove(name);
            old.setParentNode(null);
            return old;
        }

        public Entity removeEntity(String name) {
            Entity old = entities.remove(name);
            old.setParentNode(null);
            return old;
        }

        public Light removeLight(String name) {
            Light old = lights.remove(name);
            old.setParentNode(null);
            return old;
        }

        public Camera removeCamera(String name) {
            Camera old = cameras.remove(name);
            old.setParentNode(null);
            return old;
        }
    }
}
