function randomPatient(e,seed) {
  e.set("/pid", "...");
}

function send(e) {
  e.set("/msgid", randomId());
  e.set("/date", now());
  e.send(host, 123); // stores msg and response
  sleep(1000);
}

loadScript("barts.js");
m1 = loadMessage("order1.txt");
randomPid(m1);
randomOrder(m1);
send(m1);
randomOrder(m1);
send(m1);
// ...
m3 = loadMessage("merge.txt");
m3.set("/pid1", m1.get("/pid"));
m3.set("/pid2", m2.get("/pid"));
send(m3);
alert("merge sent for " + pid1 + ", " + pid2);

