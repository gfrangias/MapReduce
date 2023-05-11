async function fetchFiles() {
    const response = await fetch('list_files.php');
    if (response.ok) {
      const data = await response.json();
      populateFileTable('mapreduce-table', data.mapreduceFiles);
      populateFileTable('dataset-table', data.datasetFiles);
    } else {
      console.error('Error fetching files:', response.statusText);
    }
  }

  $(document).ready(function() {
    $('#myTable').DataTable({
      order: [[0, 'asc']] // Sort by the first column (Name) in ascending order
    });
  });

  function populateFileTable(tableId, files) {
    const tableBody = document.getElementById(tableId).querySelector('tbody');
    tableBody.innerHTML = '';

    files.forEach((file) => {
      const row = document.createElement('tr');
      const filenameCell = document.createElement('td');
      const fileSizeCell = document.createElement('td');
      const actionCell = document.createElement('td');
      actionCell.className = 'text-center';
  
      const filenameLink = document.createElement('a');
      filenameLink.href = file.path;
      filenameLink.target = '_blank';
      filenameLink.rel = 'noopener noreferrer';
      filenameLink.textContent = file.name;
  
      filenameCell.appendChild(filenameLink);
      fileSizeCell.textContent = formatFileSize(file.size);
  
      const deleteBtnWrapper = document.createElement('div');
      deleteBtnWrapper.className = '';
      const deleteBtn = document.createElement('button');
      deleteBtn.className ='btn btn-danger btn-short fas fa-trash-alt';
      deleteBtn.addEventListener('click', async () => {
        await deleteFile(tableId, file.name);
        fetchFiles(); // Refresh the file list after deletion
      });
  
      deleteBtnWrapper.appendChild(deleteBtn);
      actionCell.appendChild(deleteBtnWrapper);
  
      row.appendChild(filenameCell);
      row.appendChild(fileSizeCell);
      row.appendChild(actionCell);
      tableBody.appendChild(row);
    });
  }
  
  function formatFileSize(size) {
    const units = ['B', 'KB', 'MB', 'GB'];
    let i = 0;
    while (size >= 1024 && i < units.length - 1) {
      size /= 1024;
      i++;
    }
    return `${size.toFixed(2)} ${units[i]}`;
  }
  
  async function deleteFile(fileType, fileName) {
    const response = await fetch('delete_file.php', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        fileType: fileType,
        fileName: fileName,
      }),
    });
  
    if (!response.ok) {
      console.error('Error deleting file:', response.statusText);
    }
  }

  // Call fetchFiles() when the page loads
  document.addEventListener('DOMContentLoaded', fetchFiles);



  const upload_Files = document.getElementById('upload-btn');
  upload_Files.addEventListener('click', async (event) => {
    event.preventDefault();
    const uploadBtn = document.getElementById('upload-btn');
    uploadBtn.disabled = true;

    // Get the file input elements
    const mapreduceFileInput = document.getElementById('mapreduce-file');
    const datasetFileInput = document.getElementById('dataset-file');
  
    // Create FormData instances for the MapReduce and Dataset files
    const mapreduceFormData = new FormData();
    const datasetFormData = new FormData();
  
    // Append the uploaded files to the FormData instances
    mapreduceFormData.append('mapreduce-file', mapreduceFileInput.files[0]);
    datasetFormData.append('dataset-file', datasetFileInput.files[0]);

    const onUploadProgress = (progressEvent) => {
      const percentage = Math.round((progressEvent.loaded / Math.ceil(progressEvent.total)) * 100);
      const progressBar = document.querySelector('.progress-bar');
  
      progressBar.style.width = `${percentage}%`;
      progressBar.setAttribute('aria-valuenow', percentage);
    };
  
    try {
      const mapreduceResponse = await axios.post('upload.php', mapreduceFormData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress,
      });
  
      const datasetResponse = await axios.post('upload.php', datasetFormData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress,
      });
      
      if (mapreduceResponse.status === 200 || datasetResponse.status === 200) {
        fetchFiles(); // Refresh the file tables after successful upload
        console.log("I fetched...");
        // Reset the progress bar

        setTimeout(() => {
          const progressBar = document.getElementById("prbar")
  
          progressBar.style.width = '0%';
          progressBar.setAttribute('aria-valuenow', 0);
        }, 2000);

        const mapreduceFileInput = document.getElementById('mapreduce-file');
        const datasetFileInput = document.getElementById('dataset-file');
        mapreduceFileInput.value = '';
        datasetFileInput.value = '';


      } else {
        console.error('Error uploading files');
      }
    } catch (error) {
      console.error('Error uploading files:', error);
    }
  });
  
  
      const mapreduceFileInput = document.getElementById('mapreduce-file');
      const datasetFileInput = document.getElementById('dataset-file');
      const uploadBtn = document.getElementById('upload-btn');
  
      mapreduceFileInput.addEventListener('change', updateUploadButtonStatus);
      datasetFileInput.addEventListener('change', updateUploadButtonStatus);
  
      function updateUploadButtonStatus() {
        if (mapreduceFileInput.files.length === 0 && datasetFileInput.files.length === 0) {
          uploadBtn.disabled = true;
        } else {
          uploadBtn.disabled = false;
        }
      }