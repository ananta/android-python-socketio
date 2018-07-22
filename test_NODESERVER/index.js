const express = require('express');
const app = express();
const http = require('http').Server(app);
var io = require('socket.io')(http);
const PORT = 3000;

app.get('/',(req,res) => {
  res.send('<h1>Hello World</h1>');
});


io.on('connection', (socket) => {
  console.log('Device Connected');

  socket.on('disconnect',()=> {
    console.log('Device Disconnected');
  })
  socket.on('location', (data) => {
    console.log(data)
  })
});

http.listen(PORT, () => {
  console.log('Listening On Port :'+ PORT);
})
