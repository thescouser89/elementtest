# elementtest

This project uses Quarkus, the Supersonic Subatomic Java Framework. It is used to demonstrate
a bug in Hibernate.

Quarkus 3.17.4 is using Hibernate 6.6.3.Final

# Bug
When using `@ElementCollection`, the order of deletions looks wrong. From the app output:

We have a table named `FinalLog`, which has as field a `String` set annotated with `@ElementCollection`.

```java
@Entity
public class FinalLog extends PanacheEntity {

    @ElementCollection
    public Set<String> tags;
}
```
As per [HHH-5529](https://hibernate.atlassian.net/browse/HHH-5529), a deletion on a `FinalLog` will trigger a deletion on the collection table also.

We try to delete using:
```java
    public String delete() {
        Parameters parameters =Parameters.with("tag", "yu");
        FinalLog.delete("from FinalLog where :tag in elements(tags)", parameters);
        return "here";
    }
```

The Hibernate logs though seems to delete from the finallog_tags table first, and then delete from the finallog table by querying for the already deleted tags (which are now gone), and thus the finallog row is never deleted.

```
[Hibernate] 
    delete 
    from
        FinalLog_tags to_delete_ 
    where
        to_delete_.FinalLog_id in (select
            fl1_0.id 
        from
            FinalLog fl1_0 
        where
            ? in (select
                t1_0.tags 
            from
                FinalLog_tags t1_0 
            where
                fl1_0.id=t1_0.FinalLog_id))
[Hibernate] 
    delete 
    from
        FinalLog fl1_0 
    where
        ? in (select
            t1_0.tags 
        from
            FinalLog_tags t1_0 
        where
            fl1_0.id=t1_0.FinalLog_id)
```

I was expecting Hibernate to instead delete from the finallog table first, and then in the finallog_tags table (for the cascade delete operation).

Here's the entry in the database after deletion:
```
testme=# select * from finallog; select * from finallog_tags;
 id 
----
  1
(1 row)

 finallog_id | tags 
-------------+------
(0 rows)

testme=# 
```

The rows in finallog_tags are gone, but not the row in finallog table.


# Running the reproducer
It's a quarkus app. Adjust the `src/main/resources/application.properties` to point to a test Postgres database. Also please adjust the username and password.

## Building
```
$ mvn clean install -DskipTests=true
```

## Running
```
$ java -jar target/quarkus-app/quarkus-run.jar
```

## Inserting data
```
$ curl http://localhost:8080/create
```

Verify in your database that rows have been created

```
$ psql <db>
$ select * from finallog; select * from finallog_tags;
```

## Deleting data
```
$ curl http://localhost:8080/delete
```

Now the row in finallog_tags are deleted but not in finallog
```
$ psql <db>
$ select * from finallog; select * from finallog_tags;
```
You can also check the Quarkus output for the SQL being submitted to the DB. The order of the SQL statement looks wrong. I don't know if there should also be a cascade relation setup with finallog_tags table.
