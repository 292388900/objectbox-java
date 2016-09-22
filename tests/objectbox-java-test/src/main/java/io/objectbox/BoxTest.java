package io.objectbox;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BoxTest extends AbstractObjectBoxTest {

    private Box<TestEntity> box;

    @Before
    public void setUpBox() {
        box = getTestEntityBox();
    }

    @Test
    public void testPutAndGet() {
        TestEntity entity = new TestEntity();
        entity.setSimpleInt(1977);
        long key = box.put(entity);
        assertTrue(key != 0);
        assertEquals(key, entity.getId());

        TestEntity entityRead = box.get(key);
        assertNotNull(entityRead);
        assertEquals(1977, entityRead.getSimpleInt());
    }

    @Test
    public void testPutGetUpdateGetRemove() {
        // create an entity
        TestEntity entity = new TestEntity();
        entity.setSimpleInt(1977);
        entity.setSimpleLong(54321);
        String value1 = "lulu321";
        entity.setSimpleString(value1);
        long key = box.put(entity);

        // get it
        TestEntity entityRead = box.get(key);
        assertNotNull(entityRead);
        assertEquals(1977, entityRead.getSimpleInt());
        assertEquals(54321, entityRead.getSimpleLong());
        assertEquals(value1, entityRead.getSimpleString());

        // put with changed values
        String value2 = "lala123";
        entityRead.setSimpleString(value2);
        entityRead.setSimpleLong(12345);
        box.put(entityRead);

        // get the changed entity
        entityRead = box.get(key);
        assertNotNull(entityRead);
        assertEquals(1977, entityRead.getSimpleInt());
        assertEquals(12345, entityRead.getSimpleLong());
        assertEquals(value2, entityRead.getSimpleString());

        // and remove it
        box.remove(key);
        assertNull(box.get(key));
    }

    @Test
    public void testPutManyAndGetAll() {
        List<TestEntity> entities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TestEntity entity = new TestEntity();
            entity.setSimpleInt(2000 + i);
            entities.add(entity);
        }
        box.put(entities);
        assertEquals(entities.size(), box.count());

        List<TestEntity> entitiesRead = box.getAll();
        assertEquals(entities.size(), entitiesRead.size());

        for (int i = 0; i < entities.size(); i++) {
            assertEquals(2000 + i, entitiesRead.get(i).getSimpleInt());
        }
    }

    @Test
    public void testRemoveMany() {
        List<TestEntity> entities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TestEntity entity = new TestEntity();
            entity.setSimpleInt(2000 + i);
            entities.add(entity);
        }
        box.put(entities);
        assertEquals(entities.size(), box.count());

        box.remove(entities.get(1));
        assertEquals(entities.size() - 1, box.count());
        box.remove(entities.get(4), entities.get(5));
        assertEquals(entities.size() - 3, box.count());
        List<TestEntity> entitiesRemove = new ArrayList<>();
        entitiesRemove.add(entities.get(2));
        entitiesRemove.add(entities.get(8));
        entitiesRemove.add(entities.get(7));
        box.remove(entitiesRemove);
        assertEquals(entities.size() - 6, box.count());

        List<TestEntity> entitiesRead = box.getAll();
        assertEquals(entities.size() - 6, entitiesRead.size());

        assertEquals(2000, entitiesRead.get(0).getSimpleInt());
        assertEquals(2003, entitiesRead.get(1).getSimpleInt());
        assertEquals(2006, entitiesRead.get(2).getSimpleInt());
        assertEquals(2009, entitiesRead.get(3).getSimpleInt());

        box.removeAll();
        assertEquals(0, box.count());
    }

    @Test
    public void testRunInTx() {
        final long[] counts = {0, 0};
        store.runInTx(new Runnable() {
            @Override
            public void run() {
                box.put(new TestEntity());
                counts[0] = box.count();
                box.put(new TestEntity());
                counts[1] = box.count();
            }
        });
        assertEquals(1, counts[0]);
        assertEquals(2, counts[1]);
        assertEquals(2, box.count());
    }

    @Test
    public void testPutAndGetTwoEntities() {
        store.close();
        store = createBoxStoreBuilderWithTwoEntities(false).build();
        box = store.boxFor(TestEntity.class);

        TestEntity entity = new TestEntity();
        entity.setSimpleInt(1977);
        long key = box.put(entity);
        TestEntity entityRead = box.get(key);
        assertEquals(1977, entityRead.getSimpleInt());

        Box<TestEntityMinimal> box2 = store.boxFor(TestEntityMinimal.class);
        TestEntityMinimal entity2 = new TestEntityMinimal();
        entity2.setText("foo");
        long key2 = box2.put(entity2);
        TestEntityMinimal entity2Read = box2.get(key2);
        assertEquals("foo", entity2Read.getText());
    }

    @Test
    public void testTwoReaders() {
        store.close();
        store = createBoxStoreBuilderWithTwoEntities(false).build();
        box = store.boxFor(TestEntity.class);
        box.count();

        Box<TestEntityMinimal> box2 = store.boxFor(TestEntityMinimal.class);
        box2.count();
        box.count();
    }

}