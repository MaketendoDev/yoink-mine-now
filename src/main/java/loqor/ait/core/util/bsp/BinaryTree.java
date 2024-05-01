package loqor.ait.core.util.bsp;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import loqor.ait.core.util.vortex.VortexData;
import loqor.ait.core.util.vortex.VortexNode;
import net.minecraft.util.math.Vec3d;
import java.nio.ByteBuffer;


public class BinaryTree {
    protected Node rootNode;

    public BTreeInorderIterator iterator() {
        return new BTreeInorderIterator(this.getRootNode());
    }

    public int byteSize() {
        return Node.getChildrenCount(this.getRootNode()) * 3 * Long.BYTES;
    }

    public static class Node {
        Node left;
        Node right;
        Vec3d pos;
        Vec3d ptrToLeft;
        Vec3d ptrToRight;

        public Node(Vec3d pos) {
            this.pos = pos;
            this.left = null;
            this.right = null;
            this.ptrToLeft = null;
            this.ptrToRight = null;
        }

        private static Vec3d dir(Vec3d from, Vec3d to) {
            return new Vec3d(to.x - from.x, to.y - from.y, to.z - from.z).normalize();
        }

        public void addLeft(Vec3d data) {
            this.left = new Node(data);
            this.ptrToLeft = dir(this.pos, data);
        }

        public void addRight(Vec3d data) {
            this.right = new Node(data);
            this.ptrToRight = dir(this.pos, data);
        }

        public Node getLeft() {
            return this.left;
        }

        public Node getRight() {
            return this.right;
        }

        public Vec3d getPos() {
            return this.pos;
        }

        public Vec3d getPtrToLeft() {
            return ptrToLeft;
        }

        public Vec3d getPtrToRight() {
            return ptrToRight;
        }

        public boolean isLeaf() {
            return this.left == null && this.right == null;
        }

        public static int getChildrenCount(Node node) {
            if (node == null)
                return 0;
            int i = 1;

            i += getChildrenCount(node.getLeft());
            i += getChildrenCount(node.getRight());

            return i;
        }
    };

    public BinaryTree(Vec3d root_data) {
        this.rootNode = new Node(root_data);
    }

    public Node getRootNode() {
        return this.rootNode;
    }

    public byte[] toByteArray() {
        BTreeInorderIterator it = new BTreeInorderIterator(this.getRootNode());
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        Node node = this.getRootNode();

        while (node != null) {
            VortexNode vnode = new VortexNode(node);
            vnode.serialize(out);
            node = it.next();
        }
        return out.toByteArray();
    }
}
