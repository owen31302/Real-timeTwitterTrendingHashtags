const cassandra = require('cassandra-driver');

const client = new cassandra.Client({
  contactPoints: ['127.0.0.1'],
  keyspace: 'twitterkeyspace'
});

module.exports = () => {
  client.execute(
    "SELECT * FROM twitter WHERE time = '2017-12-17 00:10:00+0000'",
    (err, result) => {
      if (!err) {
        console.log('Success!!');
        // console.log(result);
        const len = result.rowLength;
        for (var i = 0; i < len; i++) {
          const row = result.rows[i];
          const status = row.status[0];
          const state = status.get(0);
          const tag = status.get(1).get(0);
          const val = status.get(1).get(1);
          console.log(state + ' ' + tag + ' ' + val);
        }
      } else {
        console.log(err);
      }
    }
  );
};
